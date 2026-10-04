package com.necroware.terminusplayer.playback

import android.content.Context
import android.media.AudioFormat
import android.media.AudioManager
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.audio.AudioSink
import androidx.media3.exoplayer.audio.DefaultAudioSink
import kotlinx.coroutines.*
import javax.inject.Inject

/**
 * Manages two ExoPlayer instances for true overlapping crossfades,
 * with hardware offload and USB-DAC crash recovery support.
 */
@UnstableApi
class AudioEngine(
    private val context: Context,
    private val onPlayerSwapped: (ExoPlayer) -> Unit
) {

    val decibelProcessor = DecibelAudioProcessor()
    private var isBitPerfectDirectMode = false

    private val audioAttributes = AudioAttributes.Builder()
        .setUsage(C.USAGE_MEDIA)
        .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
        .build()

    @OptIn(UnstableApi::class)
    private fun createConfiguredPlayer(): ExoPlayer {
        val renderersFactory = object : DefaultRenderersFactory(context) {
            override fun buildAudioSink(
                context: Context,
                enableFloatOutput: Boolean,
                enableAudioTrackPlaybackParams: Boolean
            ): AudioSink {
                val sinkBuilder = DefaultAudioSink.Builder(context)
                    .setEnableFloatOutput(true)

                if (isBitPerfectDirectMode) {
                    // Bypass software speed/pitch resampling algorithms for bit-perfect output
                    sinkBuilder.setEnableAudioTrackPlaybackParams(false)
                } else {
                    sinkBuilder.setAudioProcessors(arrayOf(decibelProcessor))
                }

                return sinkBuilder.build()
            }
        }

        return ExoPlayer.Builder(context, renderersFactory).build().apply {
            setAudioAttributes(audioAttributes, true)
            setHandleAudioBecomingNoisy(true)
            addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    Log.w("AudioEngine", "Player error encountered: ${error.errorCodeName}", error)
                    if (isAudioSinkError(error)) {
                        recoverAudioSinkError(this@apply)
                    }
                }
            })
        }
    }

    private var playerA = createConfiguredPlayer()
    private var playerB = createConfiguredPlayer()

    private var activePlayer: ExoPlayer = playerA
    private var standbyPlayer: ExoPlayer = playerB

    private var crossfadeJob: Job? = null
    private var isCrossfading = false
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)

    fun getActivePlayer(): ExoPlayer = activePlayer
    fun isCrossfading(): Boolean = isCrossfading

    /**
     * Query device capabilities via AudioManager before requesting hardware offload.
     * Fall back to software pipeline if unsupported.
     */
    @OptIn(UnstableApi::class)
    fun configureAudioOffload(sampleRate: Int, channelCount: Int, encoding: Int) {
        if (sampleRate <= 0 || channelCount <= 0) return

        val channelMask = when (channelCount) {
            1 -> AudioFormat.CHANNEL_OUT_MONO
            2 -> AudioFormat.CHANNEL_OUT_STEREO
            else -> AudioFormat.CHANNEL_OUT_STEREO
        }

        val audioFormatBuilder = AudioFormat.Builder()
            .setSampleRate(sampleRate)
            .setChannelMask(channelMask)

        if (encoding != Format.NO_VALUE && encoding != 0) {
            audioFormatBuilder.setEncoding(encoding)
        }

        val audioFormat = audioFormatBuilder.build()

        val frameworkAttributes = android.media.AudioAttributes.Builder()
            .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        val canOffload = try {
            AudioManager.isOffloadedPlaybackSupported(audioFormat, frameworkAttributes)
        } catch (e: Exception) {
            false
        }

        val offloadPreferences = TrackSelectionParameters.AudioOffloadPreferences.Builder()
            .setAudioOffloadMode(
                if (canOffload)
                    TrackSelectionParameters.AudioOffloadPreferences.AUDIO_OFFLOAD_MODE_ENABLED
                else
                    TrackSelectionParameters.AudioOffloadPreferences.AUDIO_OFFLOAD_MODE_DISABLED
            )
            .build()

        val offloadParameters = activePlayer.trackSelectionParameters
            .buildUpon()
            .setAudioOffloadPreferences(offloadPreferences)
            .build()

        playerA.trackSelectionParameters = offloadParameters
        playerB.trackSelectionParameters = offloadParameters
    }

    @OptIn(UnstableApi::class)
    fun configureUsbExclusiveHq(enabled: Boolean) {
        isBitPerfectDirectMode = enabled
        val offloadMode = if (enabled) {
            TrackSelectionParameters.AudioOffloadPreferences.AUDIO_OFFLOAD_MODE_ENABLED
        } else {
            TrackSelectionParameters.AudioOffloadPreferences.AUDIO_OFFLOAD_MODE_DISABLED
        }

        val offloadPreferences = TrackSelectionParameters.AudioOffloadPreferences.Builder()
            .setAudioOffloadMode(offloadMode)
            .build()

        val offloadParameters = activePlayer.trackSelectionParameters
            .buildUpon()
            .setAudioOffloadPreferences(offloadPreferences)
            .build()

        playerA.trackSelectionParameters = offloadParameters
        playerB.trackSelectionParameters = offloadParameters
    }

    /**
     * Safely handles USB-DAC connection or disconnection while audio is playing
     * to prevent underflow crashes or AudioTrack initialization exceptions.
     */
    fun handleUsbDacChange() {
        scope.launch(Dispatchers.Main) {
            val player = activePlayer
            val wasPlaying = player.isPlaying
            val currentPos = player.currentPosition

            try {
                player.pause()
                player.seekTo(currentPos)
                player.prepare()
                if (wasPlaying) {
                    player.play()
                }
            } catch (e: Exception) {
                Log.e("AudioEngine", "Error handling USB DAC change, performing soft reset", e)
                try {
                    player.prepare()
                } catch (e2: Exception) {
                    Log.e("AudioEngine", "Failed player prepare during USB DAC recovery", e2)
                }
            }
        }
    }

    private fun isAudioSinkError(error: PlaybackException): Boolean {
        return error.errorCode == PlaybackException.ERROR_CODE_AUDIO_TRACK_INIT_FAILED ||
               error.errorCode == PlaybackException.ERROR_CODE_AUDIO_TRACK_WRITE_FAILED ||
               error.errorCode == PlaybackException.ERROR_CODE_DECODER_INIT_FAILED
    }

    private fun recoverAudioSinkError(player: ExoPlayer) {
        scope.launch(Dispatchers.Main) {
            val wasPlaying = player.playWhenReady
            val pos = player.currentPosition
            Log.i("AudioEngine", "Attempting AudioSink crash recovery at position $pos")
            try {
                player.stop()
                player.seekTo(pos)
                player.prepare()
                player.playWhenReady = wasPlaying
            } catch (e: Exception) {
                Log.e("AudioEngine", "AudioSink recovery failed", e)
            }
        }
    }

    fun executeCrossfade(
        items: List<MediaItem>,
        nextIndex: Int,
        crossfadeDurationMs: Long = 4500L,
        onTrackSwapped: (ExoPlayer) -> Unit
    ) {
        if (isCrossfading || items.isEmpty() || nextIndex !in items.indices) return
        isCrossfading = true

        crossfadeJob?.cancel()
        crossfadeJob = scope.launch(Dispatchers.Default) {
            val fadeOutPlayer = activePlayer
            val fadeInPlayer = standbyPlayer

            // 1. Prepare standby player off the main thread with matching settings
            withContext(Dispatchers.Main) {
                fadeInPlayer.shuffleModeEnabled = fadeOutPlayer.shuffleModeEnabled
                fadeInPlayer.repeatMode = fadeOutPlayer.repeatMode
                fadeInPlayer.setMediaItems(items, nextIndex, 0L)
                fadeInPlayer.volume = 0f
                fadeInPlayer.prepare()
                fadeInPlayer.playWhenReady = true
                fadeInPlayer.play()
            }

            val steps = (crossfadeDurationMs / 50L).toInt().coerceAtLeast(1)

            // 2. Smooth Logarithmic Volume Ramping Loop
            for (i in 0..steps) {
                val progress = i.toFloat() / steps

                // Exponential fade curve for natural human ear perception
                val fadeOutVol = (1f - progress) * (1f - progress)
                val fadeInVol = progress * progress

                withContext(Dispatchers.Main) {
                    fadeOutPlayer.volume = fadeOutVol
                    fadeInPlayer.volume = fadeInVol
                }

                delay(50L)
            }

            // 3. Finalize Handshake & Swap Player Roles
            withContext(Dispatchers.Main) {
                fadeOutPlayer.pause()
                fadeOutPlayer.volume = 1f
                fadeOutPlayer.clearMediaItems()

                activePlayer = fadeInPlayer
                standbyPlayer = fadeOutPlayer

                isCrossfading = false
                onTrackSwapped(activePlayer)
            }
        }
    }

    fun crossfadeToTrack(nextItem: MediaItem, durationMs: Long = 4500L) {
        executeCrossfade(listOf(nextItem), 0, durationMs) { newPlayer ->
            onPlayerSwapped(newPlayer)
        }
    }

    fun release() {
        crossfadeJob?.cancel()
        playerA.release()
        playerB.release()
        scope.cancel()
    }

    companion object {
        const val ACTION_CROSSFADE_NEXT = "com.necroware.terminusplayer.CROSSFADE_NEXT"
    }
}
