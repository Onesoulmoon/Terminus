package com.necroware.terminusplayer.playback

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionToken
import com.necroware.terminusplayer.data.model.Song
import com.necroware.terminusplayer.util.albumIdOrNull
import com.necroware.terminusplayer.util.toMediaItem
import com.necroware.terminusplayer.util.toMediaItems
import com.necroware.terminusplayer.util.bitrateKbpsOrNegative
import com.necroware.terminusplayer.util.estimateKbpsFromSize
import com.necroware.terminusplayer.util.isLosslessFormat
import com.necroware.terminusplayer.util.sizeBytesOrZero
import com.necroware.terminusplayer.util.toBitDepthLabel
import com.necroware.terminusplayer.util.toCodecLabel
import com.necroware.terminusplayer.util.toSampleRateLabel
import com.necroware.terminusplayer.util.selectedAudioFormat
import com.necroware.terminusplayer.util.toAudioFormatLabel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class NowPlayingState(
    val mediaId: String? = null,
    val title: String = "",
    val artist: String = "",
    val album: String = "",
    val albumId: Long = -1L,
    val artworkUri: String? = null,
    val durationMs: Long = 0L,
    val positionMs: Long = 0L,
    val isPlaying: Boolean = false,
    val shuffleEnabled: Boolean = false,
    val repeatMode: Int = Player.REPEAT_MODE_OFF,
    val audioFormatLabel: String = "—",
    val codecLabel: String = "—",
    val sampleRateLabel: String = "—",
    val isLossless: Boolean = false,
    val bitDepthLabel: String = "—",
    val bitrateKbps: Int = -1,
    val sizeBytes: Long = 0L
)

@Singleton
class PlaybackController @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private var controller: MediaController? = null
    private var connecting = false
    private val pendingCommands = ArrayDeque<MediaController.() -> Unit>()

    private val _state = MutableStateFlow(NowPlayingState())
    val state: StateFlow<NowPlayingState> = _state

    private fun resolveKbps(format: Format?, sizeBytes: Long, durationMs: Long): Int {
        val kbpsFromFormat = format?.bitrateKbpsOrNegative() ?: -1
        if (kbpsFromFormat > 0) return kbpsFromFormat
        return estimateKbpsFromSize(sizeBytes, durationMs)
    }

    private val listener = object : Player.Listener {
        override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
            updateFromController()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _state.value = _state.value.copy(isPlaying = isPlaying)
        }

        override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
            _state.value = _state.value.copy(shuffleEnabled = shuffleModeEnabled)
        }

        override fun onRepeatModeChanged(repeatMode: Int) {
            _state.value = _state.value.copy(repeatMode = repeatMode)
        }

        override fun onTracksChanged(tracks: Tracks) {
            val format = tracks.selectedAudioFormat()
            val currentSize = _state.value.sizeBytes
            val currentDuration = _state.value.durationMs
            val kbps = resolveKbps(format, currentSize, currentDuration)

            _state.value = _state.value.copy(
                audioFormatLabel = format?.toAudioFormatLabel() ?: "—",
                codecLabel = format?.toCodecLabel() ?: "—",
                sampleRateLabel = format?.toSampleRateLabel() ?: "—",
                isLossless = format?.isLosslessFormat() ?: false,
                bitDepthLabel = format?.toBitDepthLabel() ?: "—",
                bitrateKbps = kbps
            )
        }
    }

    fun connect(onReady: () -> Unit = {}) {
        if (controller?.isConnected == true) {
            onReady()
            return
        }
        if (connecting) return
        connecting = true
        val sessionToken = SessionToken(context, ComponentName(context, MusicService::class.java))
        val future = MediaController.Builder(context, sessionToken).buildAsync()
        future.addListener({
            try {
                controller = future.get().also { it.addListener(listener) }
                updateFromController()
                val queued = pendingCommands.toList()
                pendingCommands.clear()
                val readyController = controller
                if (readyController != null) {
                    queued.forEach { command -> readyController.command() }
                }
                onReady()
            } catch (e: Exception) {
                Log.e("PlaybackController", "Failed to connect to MediaController", e)
            } finally {
                connecting = false
            }
        }, context.mainExecutor)
    }

    private fun enqueueOrRun(command: MediaController.() -> Unit) {
        val c = controller
        if (c?.isConnected == true) {
            c.command()
        } else {
            pendingCommands.addLast(command)
            connect()
        }
    }

    fun release() {
        pendingCommands.clear()
        connecting = false
        controller?.removeListener(listener)
        controller?.release()
        controller = null
    }

    fun playSongs(items: List<MediaItem>, startIndex: Int) = enqueueOrRun {
        // Enforce retaining shuffle settings safely on manual item overrides
        val targetShuffle = shuffleModeEnabled
        setMediaItems(items, startIndex, 0L)
        shuffleModeEnabled = targetShuffle
        prepare()
        play()
    }

    fun playTrackFromSearch(searchSelectedTrack: Song, masterPlaylist: List<Song>) = enqueueOrRun {
        // Guarantee index 0 is the exact selected track to prevent shuffle index mismatches
        val reorderedQueue = listOf(searchSelectedTrack) + masterPlaylist.filter { it.id != searchSelectedTrack.id }
        val targetShuffle = shuffleModeEnabled
        setMediaItems(reorderedQueue.toMediaItems(), 0, 0L)
        shuffleModeEnabled = targetShuffle
        prepare()
        play()
    }

    fun playExternalUri(uri: Uri) = enqueueOrRun {
        setMediaItem(
            MediaItem.Builder()
                .setMediaId(uri.toString())
                .setUri(uri)
                .build()
        )
        prepare()
        play()
    }

    fun skipWithCrossfade(nextTrackUri: String) = enqueueOrRun {
        val args = Bundle().apply {
            putString("NEXT_URI", nextTrackUri)
        }
        sendCustomCommand(
            SessionCommand(AudioEngine.ACTION_CROSSFADE_NEXT, Bundle.EMPTY),
            args
        )
    }

    fun togglePlayPause() = enqueueOrRun {
        if (isPlaying) pause() else play()
    }

    fun skipToNext() = enqueueOrRun { seekToNext() }
    fun skipToPrevious() = enqueueOrRun { seekToPrevious() }
    fun seekTo(positionMs: Long) = enqueueOrRun { seekTo(positionMs) }

    fun seekForward5s() = enqueueOrRun {
        val target = (currentPosition + 5000L).coerceAtMost(duration.coerceAtLeast(0L))
        seekTo(target)
    }

    fun seekBackward5s() = enqueueOrRun {
        val target = (currentPosition - 5000L).coerceAtLeast(0L)
        seekTo(target)
    }

    fun addSongToQueue(song: Song) = enqueueOrRun {
        addMediaItem(song.toMediaItem())
    }

    fun playSongNext(song: Song) = enqueueOrRun {
        val nextIndex = if (mediaItemCount > 0) currentMediaItemIndex + 1 else 0
        addMediaItem(nextIndex, song.toMediaItem())
    }

    fun toggleShuffle() = enqueueOrRun {
        shuffleModeEnabled = !shuffleModeEnabled
    }

    fun cycleRepeatMode() = enqueueOrRun {
        repeatMode = when (repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
    }

    fun currentPositionMs(): Long = if (controller?.isConnected == true) controller!!.currentPosition else 0L
    fun durationMs(): Long = if (controller?.isConnected == true) controller!!.duration.coerceAtLeast(0L) else 0L

    private fun updateFromController() {
        val c = controller ?: return
        if (!c.isConnected) return
        
        val metadata = c.mediaMetadata
        val currentItem = try { c.currentMediaItem } catch (e: Exception) { null }
        val duration = try { c.duration } catch (e: Exception) { 0L }
        val isPlaying = try { c.isPlaying } catch (e: Exception) { false }
        val currentSize = metadata.sizeBytesOrZero()
        val currentDuration = duration.coerceAtLeast(0L)

        val resolvedKbps = if (_state.value.bitrateKbps > 0) {
            _state.value.bitrateKbps
        } else {
            estimateKbpsFromSize(currentSize, currentDuration)
        }

        _state.value = _state.value.copy(
            mediaId = currentItem?.mediaId,
            title = metadata.title?.toString().orEmpty(),
            artist = metadata.artist?.toString().orEmpty(),
            album = metadata.albumTitle?.toString().orEmpty(),
            albumId = metadata.albumIdOrNull() ?: -1L,
            artworkUri = metadata.artworkUri?.toString(),
            sizeBytes = currentSize,
            durationMs = currentDuration,
            bitrateKbps = resolvedKbps,
            isPlaying = isPlaying,
            shuffleEnabled = c.shuffleModeEnabled,
            repeatMode = c.repeatMode
        )
    }
}
