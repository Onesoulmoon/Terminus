package com.necroware.terminusplayer.playback

import android.app.PendingIntent
import android.content.Intent
import android.os.SystemClock
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.*
import com.necroware.terminusplayer.MainActivity
import com.necroware.terminusplayer.R
import com.necroware.terminusplayer.data.prefs.CrossfadeSettings
import com.necroware.terminusplayer.data.prefs.EqualizerSettings
import com.necroware.terminusplayer.data.prefs.UserPreferencesRepository
import com.necroware.terminusplayer.data.repository.MusicRepository
import com.necroware.terminusplayer.data.repository.StatsRepository
import com.necroware.terminusplayer.util.albumIdOrNull
import com.necroware.terminusplayer.util.selectedAudioFormat
import com.necroware.terminusplayer.util.toMediaItem
import com.necroware.terminusplayer.util.toMediaItems
import com.necroware.terminusplayer.widget.TerminusWidgetProvider
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class MusicService : MediaSessionService() {

    @Inject
    lateinit var statsRepository: StatsRepository

    @Inject
    lateinit var preferencesRepository: UserPreferencesRepository

    @Inject
    lateinit var musicRepository: MusicRepository

    @Inject
    lateinit var visualizerHelper: AudioVisualizerHelper

    @Inject
    lateinit var usbDacManager: UsbDacManager

    private var mediaSession: MediaSession? = null
    private lateinit var audioEngine: AudioEngine

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mainScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val equalizerController = EqualizerController()
    private var equalizerSettings = EqualizerSettings()
    private var crossfadeSettings = CrossfadeSettings()
    private var fadeInJob: Job? = null
    private var fadeTickerJob: Job? = null
    private var isCrossfadeTriggered = false
    private var savePositionJob: Job? = null
    private var widgetUpdateJob: Job? = null

    private var trackedSongId: Long? = null
    private var trackedArtist: String = ""
    private var trackedAlbum: String = ""
    private var trackedAlbumId: Long = -1L
    private var trackedStartedAtElapsedMs: Long = 0L
    private var trackedDurationMs: Long = 0L

    companion object {
        const val CUSTOM_ACTION_LIKE = "com.necroware.terminusplayer.ACTION_LIKE"
        const val CUSTOM_ACTION_SHUFFLE = "com.necroware.terminusplayer.ACTION_SHUFFLE"
    }

    private val analyticsListener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            isCrossfadeTriggered = false
            flushCurrentTrack()
            startTracking(mediaItem?.mediaMetadata, mediaItem?.mediaId)
            
            val player = audioEngine.getActivePlayer()
            val title = mediaItem?.mediaMetadata?.title?.toString() ?: "SYS_IDLE"
            val artist = mediaItem?.mediaMetadata?.artist?.toString() ?: "OFFLINE"
            TerminusWidgetProvider.pushUpdate(
                context = this@MusicService,
                trackTitle = title,
                artist = artist,
                isPlaying = player.isPlaying
            )
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            val player = audioEngine.getActivePlayer()
            val title = player.currentMediaItem?.mediaMetadata?.title?.toString() ?: "SYS_IDLE"
            val artist = player.currentMediaItem?.mediaMetadata?.artist?.toString() ?: "OFFLINE"
            TerminusWidgetProvider.pushUpdate(
                context = this@MusicService,
                trackTitle = title,
                artist = artist,
                isPlaying = player.isPlaying
            )
            if (playbackState == Player.STATE_ENDED) {
                flushCurrentTrack()
            }
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            val player = audioEngine.getActivePlayer()
            val title = player.currentMediaItem?.mediaMetadata?.title?.toString() ?: "SYS_IDLE"
            val artist = player.currentMediaItem?.mediaMetadata?.artist?.toString() ?: "OFFLINE"
            TerminusWidgetProvider.pushUpdate(
                context = this@MusicService,
                trackTitle = title,
                artist = artist,
                isPlaying = isPlaying
            )
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            serviceScope.launch { preferencesRepository.setShuffleEnabled(shuffleModeEnabled) }
            updateNotificationButtons()
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            serviceScope.launch { preferencesRepository.setRepeatMode(repeatMode) }
        }
    }

    private var isRealtimeVisualizerEnabled = false

    @OptIn(UnstableApi::class)
    private val playerListener = object : Player.Listener {
        override fun onAudioSessionIdChanged(audioSessionId: Int) {
            if (audioSessionId != C.AUDIO_SESSION_ID_UNSET) {
                if (isRealtimeVisualizerEnabled) {
                    visualizerHelper.linkToAudioSession(audioSessionId)
                } else {
                    visualizerHelper.release()
                }
                equalizerController.attachToSession(audioSessionId)
                equalizerController.setEnabled(equalizerSettings.enabled)
                equalizerController.applyEqualizerSettings(equalizerSettings)
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED || playbackState == Player.STATE_IDLE) {
                visualizerHelper.release()
            }
            if (playbackState == Player.STATE_READY) {
                val player = audioEngine.getActivePlayer()
                if (player.audioSessionId != C.AUDIO_SESSION_ID_UNSET) {
                    if (isRealtimeVisualizerEnabled) {
                        visualizerHelper.linkToAudioSession(player.audioSessionId)
                    } else {
                        visualizerHelper.release()
                    }
                }
            }
        }

        override fun onTracksChanged(tracks: Tracks) {
            val format = tracks.selectedAudioFormat()
            if (format != null) {
                audioEngine.configureAudioOffload(
                    sampleRate = format.sampleRate,
                    channelCount = format.channelCount,
                    encoding = format.pcmEncoding
                )
            }
        }
    }

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()

        audioEngine = AudioEngine(this) { newActivePlayer ->
            mediaSession?.player = newActivePlayer
            newActivePlayer.addListener(analyticsListener)
            newActivePlayer.addListener(playerListener)
            
            if (newActivePlayer.audioSessionId != C.AUDIO_SESSION_ID_UNSET) {
                visualizerHelper.linkToAudioSession(newActivePlayer.audioSessionId)
                equalizerController.attachToSession(newActivePlayer.audioSessionId)
                equalizerController.setEnabled(equalizerSettings.enabled)
                equalizerController.applyEqualizerSettings(equalizerSettings)
            }
            pushWidgetUpdate()
        }

        usbDacManager.register(
            onAttached = { device ->
                mainScope.launch {
                    audioEngine.handleUsbDacChange()
                }
            },
            onDetached = {
                mainScope.launch {
                    audioEngine.handleUsbDacChange()
                }
            }
        )

        val player = audioEngine.getActivePlayer()
        player.addListener(analyticsListener)
        player.addListener(playerListener)

        val sessionActivityIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            sessionActivityIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(pendingIntent)
            .setCallback(TerminusSessionCallback())
            .build()
            
        val notificationProvider = DefaultMediaNotificationProvider.Builder(this).build()
        notificationProvider.setSmallIcon(R.drawable.ic_terminus_status_icon)
        setMediaNotificationProvider(notificationProvider)

        updateNotificationButtons()

        serviceScope.launch {
            preferencesRepository.preferences.collect { prefs ->
                equalizerSettings = prefs.equalizer
                equalizerController.setEnabled(prefs.equalizer.enabled)
                equalizerController.applyEqualizerSettings(prefs.equalizer)

                isRealtimeVisualizerEnabled = prefs.realtimeVisualizerEnabled

                mainScope.launch {
                    if (!prefs.realtimeVisualizerEnabled) {
                        visualizerHelper.release()
                    } else {
                        val session = audioEngine.getActivePlayer().audioSessionId
                        if (session != C.AUDIO_SESSION_ID_UNSET) {
                            visualizerHelper.linkToAudioSession(session)
                        }
                    }

                    audioEngine.configureUsbExclusiveHq(prefs.usbExclusiveHqEnabled)
                    if (prefs.usbExclusiveHqEnabled) {
                        usbDacManager.requestUsbDacPermission { device ->
                            // DAC permission requested / granted
                        }
                    }
                }

                val crossfadeChanged = crossfadeSettings != prefs.crossfade
                crossfadeSettings = prefs.crossfade
                if (crossfadeChanged) {
                    mainScope.launch {
                        if (prefs.crossfade.enabled) startFadeTicker() else stopFading()
                    }
                }
            }
        }

        // Restore last session
        serviceScope.launch {
            val prefs = preferencesRepository.preferences.first()
            withContext(Dispatchers.Main) {
                val player = audioEngine.getActivePlayer()
                player.shuffleModeEnabled = prefs.shuffleEnabled
                player.repeatMode = prefs.repeatMode.coerceIn(Player.REPEAT_MODE_OFF, Player.REPEAT_MODE_ALL)
            }
            val lastId = prefs.lastPlayedSongId
            val queueIds = prefs.lastPlayedQueueIds
            
            if (lastId != null) {
                val songs = musicRepository.observeAllSongs().first()
                
                withContext(Dispatchers.Main) {
                    val player = audioEngine.getActivePlayer()
                    if (player.mediaItemCount == 0) {
                        if (queueIds.isNotEmpty()) {
                            val queueSongs = queueIds.mapNotNull { id -> songs.find { it.id == id } }
                            val startIndex = queueSongs.indexOfFirst { it.id == lastId }.coerceAtLeast(0)
                            player.setMediaItems(queueSongs.toMediaItems(), startIndex, prefs.lastPlayedPositionMs)
                        } else {
                            val song = songs.find { it.id == lastId }
                            if (song != null) {
                                player.setMediaItem(song.toMediaItem(), prefs.lastPlayedPositionMs)
                            }
                        }
                        player.prepare()
                    }
                }
            }
        }

        startSavePositionTicker()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    private inner class TerminusSessionCallback : MediaSession.Callback {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            val sessionCommands = MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS.buildUpon()
                .add(SessionCommand(CUSTOM_ACTION_LIKE, Bundle.EMPTY))
                .add(SessionCommand(CUSTOM_ACTION_SHUFFLE, Bundle.EMPTY))
                .build()
            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(sessionCommands)
                .build()
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            when (customCommand.customAction) {
                AudioEngine.ACTION_CROSSFADE_NEXT -> {
                    val nextUri = args.getString("NEXT_URI")
                    if (nextUri != null) {
                        audioEngine.crossfadeToTrack(MediaItem.fromUri(nextUri))
                    }
                }
                CUSTOM_ACTION_LIKE -> {
                    val player = audioEngine.getActivePlayer()
                    val mediaId = player.currentMediaItem?.mediaId?.toLongOrNull()
                    if (mediaId != null) {
                        serviceScope.launch {
                            musicRepository.toggleLike(mediaId)
                        }
                    }
                }
                CUSTOM_ACTION_SHUFFLE -> {
                    val player = audioEngine.getActivePlayer()
                    player.shuffleModeEnabled = !player.shuffleModeEnabled
                }
            }
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }
    }

    private fun updateNotificationButtons() {
        val likeButton = CommandButton.Builder()
            .setDisplayName("Like")
            .setIconResId(R.drawable.ic_cmd_like)
            .setSessionCommand(SessionCommand(CUSTOM_ACTION_LIKE, Bundle.EMPTY))
            .build()

        val shuffleButton = CommandButton.Builder()
            .setDisplayName("Shuffle ⇎")
            .setIconResId(R.drawable.ic_cmd_shuffle) 
            .setSessionCommand(SessionCommand(CUSTOM_ACTION_SHUFFLE, Bundle.EMPTY))
            .build()
            
        mediaSession?.setCustomLayout(listOf(likeButton, shuffleButton))
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player ?: return
        if (!player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
        super.onTaskRemoved(rootIntent)
    }

    private fun pushWidgetUpdate() {
        val player = audioEngine.getActivePlayer()
        val title = player.currentMediaItem?.mediaMetadata?.title?.toString() ?: "SYS_IDLE"
        val artist = player.currentMediaItem?.mediaMetadata?.artist?.toString() ?: "OFFLINE"
        TerminusWidgetProvider.pushUpdate(
            context = this,
            trackTitle = title,
            artist = artist,
            isPlaying = player.isPlaying
        )
    }

    override fun onDestroy() {
        usbDacManager.unregister()
        flushCurrentTrack()
        fadeInJob?.cancel()
        fadeTickerJob?.cancel()
        savePositionJob?.cancel()
        equalizerController.release()
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        audioEngine.release()
        serviceScope.cancel()
        mainScope.cancel()
        super.onDestroy()
    }

    private fun startTracking(metadata: MediaMetadata?, mediaId: String?) {
        val player = audioEngine.getActivePlayer()
        trackedSongId = mediaId?.toLongOrNull()
        trackedArtist = metadata?.artist?.toString().orEmpty()
        trackedAlbum = metadata?.albumTitle?.toString().orEmpty()
        trackedAlbumId = metadata?.albumIdOrNull() ?: -1L
        trackedStartedAtElapsedMs = SystemClock.elapsedRealtime()
        trackedDurationMs = try {
            val d = player.duration
            if (d == C.TIME_UNSET) 0L else d.coerceAtLeast(0L)
        } catch (e: Exception) { 0L }
    }

    private fun flushCurrentTrack() {
        val songId = trackedSongId ?: return
        val msPlayed = (SystemClock.elapsedRealtime() - trackedStartedAtElapsedMs).coerceAtLeast(0L)

        if (msPlayed < 3_000L) return

        val completed = trackedDurationMs > 0 && msPlayed >= (trackedDurationMs * 0.9).toLong()
        val artist = trackedArtist
        val album = trackedAlbum
        val albumId = trackedAlbumId

        serviceScope.launch {
            statsRepository.recordPlay(
                songId = songId,
                artist = artist,
                album = album,
                albumId = albumId,
                msPlayed = msPlayed,
                completed = completed
            )
        }
    }

    private fun startSavePositionTicker() {
        savePositionJob?.cancel()
        savePositionJob = serviceScope.launch {
            while (isActive) {
                delay(5000)
                val currentId = trackedSongId
                if (currentId != null) {
                    val (pos, queueIds) = withContext(Dispatchers.Main) {
                        val player = audioEngine.getActivePlayer()
                        val ids = mutableListOf<Long>()
                        for (i in 0 until player.mediaItemCount) {
                            player.getMediaItemAt(i).mediaId.toLongOrNull()?.let { ids.add(it) }
                        }
                        player.currentPosition to ids
                    }
                    preferencesRepository.setLastPlayed(currentId, pos, queueIds)
                }
            }
        }
    }

    private fun startFadeTicker() {
        fadeTickerJob?.cancel()
        fadeTickerJob = mainScope.launch {
            while (isActive) {
                delay(200L)
                val cf = crossfadeSettings
                if (!cf.enabled) continue

                val player = audioEngine.getActivePlayer()
                if (!player.isPlaying || player.mediaItemCount <= 1) continue
                if (audioEngine.isCrossfading()) continue

                // Do not trigger crossfade overlay if repeat mode is set to REPEAT_ONE
                if (player.repeatMode == Player.REPEAT_MODE_ONE) continue

                val durationMs = player.duration
                if (durationMs <= 0L) continue

                val currentPos = player.currentPosition
                val crossfadeDurationMs = cf.durationMs.coerceAtLeast(500).toLong()
                val triggerPoint = durationMs - crossfadeDurationMs

                val nextIndex = player.getNextMediaItemIndex()
                if (currentPos >= triggerPoint && triggerPoint > 0L && nextIndex != C.INDEX_UNSET) {
                    val items = mutableListOf<MediaItem>()
                    for (i in 0 until player.mediaItemCount) {
                        items.add(player.getMediaItemAt(i))
                    }
                    audioEngine.executeCrossfade(
                        items = items,
                        nextIndex = nextIndex,
                        crossfadeDurationMs = crossfadeDurationMs
                    ) { newActivePlayer ->
                        mediaSession?.player = newActivePlayer
                        newActivePlayer.addListener(analyticsListener)
                        newActivePlayer.addListener(playerListener)
                        if (newActivePlayer.audioSessionId != C.AUDIO_SESSION_ID_UNSET) {
                            visualizerHelper.linkToAudioSession(newActivePlayer.audioSessionId)
                            equalizerController.attachToSession(newActivePlayer.audioSessionId)
                            equalizerController.setEnabled(equalizerSettings.enabled)
                            equalizerController.applyEqualizerSettings(equalizerSettings)
                        }
                        pushWidgetUpdate()
                    }
                }
            }
        }
    }

    private fun stopFading() {
        fadeInJob?.cancel()
        fadeInJob = null
        fadeTickerJob?.cancel()
        fadeTickerJob = null
        isCrossfadeTriggered = false
        audioEngine.getActivePlayer().volume = 1f
    }
}
