package com.necroware.terminusplayer.playback

import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.BaseAudioProcessor
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.log10
import kotlin.math.sqrt

/**
 * Extracts real-time RMS decibel level from raw 16-bit, 24-bit, 32-bit integer,
 * and 32-bit float PCM audio buffers in ExoPlayer's audio sink pipeline.
 */
@UnstableApi
class DecibelAudioProcessor : BaseAudioProcessor() {

    private val _audioLevel = MutableStateFlow(0f)
    val audioLevel: StateFlow<Float> = _audioLevel

    @OptIn(UnstableApi::class)
    override fun onConfigure(
        inputAudioFormat: AudioProcessor.AudioFormat
    ): AudioProcessor.AudioFormat {
        return when (inputAudioFormat.encoding) {
            C.ENCODING_PCM_16BIT,
            C.ENCODING_PCM_24BIT,
            C.ENCODING_PCM_32BIT,
            C.ENCODING_PCM_FLOAT -> inputAudioFormat
            else -> AudioProcessor.AudioFormat.NOT_SET
        }
    }

    override fun queueInput(inputBuffer: ByteBuffer) {
        val remaining = inputBuffer.remaining()
        if (remaining == 0) return

        val buffer = inputBuffer.duplicate().order(ByteOrder.LITTLE_ENDIAN)
        var sumSquare = 0.0
        var sampleCount = 0

        when (inputAudioFormat.encoding) {
            C.ENCODING_PCM_16BIT -> {
                sampleCount = remaining / 2
                while (buffer.hasRemaining() && buffer.remaining() >= 2) {
                    val sample = buffer.short.toDouble() / 32768.0
                    sumSquare += sample * sample
                }
            }
            C.ENCODING_PCM_24BIT -> {
                sampleCount = remaining / 3
                while (buffer.hasRemaining() && buffer.remaining() >= 3) {
                    val b0 = buffer.get().toInt() and 0xFF
                    val b1 = buffer.get().toInt() and 0xFF
                    val b2 = buffer.get().toInt()
                    val raw = (b2 shl 16) or (b1 shl 8) or b0
                    val sample = raw.toDouble() / 8388608.0
                    sumSquare += sample * sample
                }
            }
            C.ENCODING_PCM_32BIT -> {
                sampleCount = remaining / 4
                while (buffer.hasRemaining() && buffer.remaining() >= 4) {
                    val sample = buffer.int.toDouble() / 2147483648.0
                    sumSquare += sample * sample
                }
            }
            C.ENCODING_PCM_FLOAT -> {
                sampleCount = remaining / 4
                while (buffer.hasRemaining() && buffer.remaining() >= 4) {
                    val sample = buffer.float.toDouble()
                    sumSquare += sample * sample
                }
            }
        }

        if (sampleCount > 0) {
            val rms = sqrt(sumSquare / sampleCount)
            val db = if (rms > 0) 20 * log10(rms) else -80.0

            val minDb = -40.0
            val maxDb = 0.0
            val normalized = ((db - minDb) / (maxDb - minDb)).coerceIn(0.0, 1.0).toFloat()

            _audioLevel.value = normalized
        }

        replaceOutputBuffer(remaining).put(inputBuffer).flip()
    }
}
