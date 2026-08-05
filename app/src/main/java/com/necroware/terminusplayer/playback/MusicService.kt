package com.necroware.terminusplayer.playback

import android.app.PendingIntent
import android.content.Intent
import android.os.SystemClock
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.necroware.terminusplayer.MainActivity
import com.necroware.terminusplayer.data.prefs.CrossfadeSettings
import com.necroware.terminusplayer.data.prefs.EqualizerSettings
import com.necroware.terminusplayer.data.prefs.UserPreferencesRepository
import com.necroware.terminusplayer.data.repository.MusicRepository
import com.necroware.terminusplayer.data.repository.StatsRepository
import com.necroware.terminusplayer.util.albumIdOrNull
import com.necroware.terminusplayer.util.toMediaItem
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

    private var mediaSession: MediaSession? = null
    private lateinit var player: ExoPlayer

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mainScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    private val equalizerController = EqualizerController()
    private var equalizerSettings = EqualizerSettings()
    private var crossfadeSettings = CrossfadeSettings()
    private var fadeInJob: Job? = null
    private var fadeTickerJob: Job? = null
    private var savePositionJob: Job? = null

    private var trackedSongId: Long? = null
    private var trackedArtist: String = ""
    private var trackedAlbum: String = ""
    private var trackedAlbumId: Long = -1L
    private var trackedStartedAtElapsedMs: Long = 0L
    private var trackedDurationMs: Long = 0L

    private val analyticsListener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            flushCurrentTrack()
            startTracking(mediaItem?.mediaMetadata, mediaItem?.mediaId)
            if (crossfadeSettings.enabled && reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
                startFadeIn()
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            if (playbackState == Player.STATE_ENDED) {
                flushCurrentTrack()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()

        player = ExoPlayer.Builder(this)
            .setHandleAudioBecomingNoisy(true)
            .build()
        player.addListener(analyticsListener)
        player.addAnalyticsListener(object : AnalyticsListener {
            override fun onAudioSessionIdChanged(eventTime: AnalyticsListener.EventTime, audioSessionId: Int) {
                equalizerController.attachToSession(audioSessionId)
                equalizerController.setEnabled(equalizerSettings.enabled)
                equalizerController.applyBandGains(equalizerSettings.bandGainsDb)
            }
        })

        val sessionActivityIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            sessionActivityIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        mediaSession = MediaSession.Builder(this, player)
            .setSessionActivity(pendingIntent)
            .build()

        serviceScope.launch {
            preferencesRepository.preferences.collect { prefs ->
                equalizerSettings = prefs.equalizer
                equalizerController.setEnabled(prefs.equalizer.enabled)
                equalizerController.applyBandGains(prefs.equalizer.bandGainsDb)

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
            val lastId = prefs.lastPlayedSongId
            if (lastId != null) {
                val songs = musicRepository.observeAllSongs().first()
                val song = songs.find { it.id == lastId }
                if (song != null) {
                    withContext(Dispatchers.Main) {
                        if (player.mediaItemCount == 0) {
                            player.setMediaItem(song.toMediaItem(), prefs.lastPlayedPositionMs)
                            player.prepare()
                        }
                    }
                }
            }
        }

        startSavePositionTicker()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player ?: return
        if (!player.playWhenReady || player.mediaItemCount == 0) {
            stopSelf()
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
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
        serviceScope.cancel()
        mainScope.cancel()
        super.onDestroy()
    }

    private fun startTracking(metadata: MediaMetadata?, mediaId: String?) {
        trackedSongId = mediaId?.toLongOrNull()
        trackedArtist = metadata?.artist?.toString().orEmpty()
        trackedAlbum = metadata?.albumTitle?.toString().orEmpty()
        trackedAlbumId = metadata?.albumIdOrNull() ?: -1L
        trackedStartedAtElapsedMs = SystemClock.elapsedRealtime()
        trackedDurationMs = player.duration.coerceAtLeast(0L)
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
                    val pos = withContext(Dispatchers.Main) { player.currentPosition }
                    preferencesRepository.setLastPlayed(currentId, pos)
                }
            }
        }
    }

    private fun startFadeIn() {
        fadeInJob?.cancel()
        val durationMs = crossfadeSettings.durationMs.coerceAtLeast(200)
        fadeInJob = mainScope.launch {
            val steps = 20
            val stepDelayMs = (durationMs / steps).coerceAtLeast(10).toLong()
            player.volume = 0f
            for (step in 1..steps) {
                delay(stepDelayMs)
                player.volume = (step.toFloat() / steps).coerceIn(0f, 1f)
            }
            player.volume = 1f
            fadeInJob = null
        }
    }

    private fun startFadeTicker() {
        fadeTickerJob?.cancel()
        fadeTickerJob = mainScope.launch {
            while (isActive) {
                delay(250)
                val cf = crossfadeSettings
                if (!cf.enabled) continue
                if (!player.isPlaying || player.mediaItemCount == 0) continue

                val durationMs = player.duration
                if (durationMs <= 0) continue
                val remainingMs = durationMs - player.currentPosition
                val fadeWindowMs = cf.durationMs.coerceAtLeast(200).toLong()

                if (fadeInJob == null) {
                    player.volume = if (remainingMs in 0..fadeWindowMs && player.hasNextMediaItem()) {
                        (remainingMs.toFloat() / fadeWindowMs).coerceIn(0f, 1f)
                    } else {
                        1f
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
        player.volume = 1f
    }
}
