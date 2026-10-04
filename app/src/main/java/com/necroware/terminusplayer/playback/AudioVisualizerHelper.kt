package com.necroware.terminusplayer.playback

import android.media.audiofx.Visualizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Captures live PCM waveform data from the active AudioSession.
 */
@Singleton
class AudioVisualizerHelper @Inject constructor() {

    private var visualizer: Visualizer? = null
    private val _barAmplitudes = MutableStateFlow(FloatArray(64) { 0.15f })
    val barAmplitudes: StateFlow<FloatArray> = _barAmplitudes

    private val _audioLevel = MutableStateFlow(0f)
    val audioLevel: StateFlow<Float> = _audioLevel

    // Exponential decay smoothing
    private var smoothedAmplitudes = FloatArray(64) { 0.15f }
    private val decayFactor = 0.82f // Liquid-smooth decay

    fun linkToAudioSession(audioSessionId: Int, realtimeEnabled: Boolean = true) {
        if (!realtimeEnabled || audioSessionId <= 0) {
            release()
            return
        }
        release()

        runCatching {
            visualizer = Visualizer(audioSessionId).apply {
                captureSize = Visualizer.getCaptureSizeRange()[0]
                
                setDataCaptureListener(
                    object : Visualizer.OnDataCaptureListener {
                        override fun onWaveFormDataCapture(
                            visualizer: Visualizer?,
                            waveform: ByteArray?,
                            samplingRate: Int
                        ) {
                            waveform?.let { processWaveform(it) }
                        }

                        override fun onFftDataCapture(
                            visualizer: Visualizer?,
                            fft: ByteArray?,
                            samplingRate: Int
                        ) {}
                    },
                    Visualizer.getMaxCaptureRate(), // Ultra-fast response
                    true,
                    false
                )
                this.enabled = true
            }
        }
    }

    private fun processWaveform(waveform: ByteArray) {
        val barCount = 64
        val step = (waveform.size / barCount).coerceAtLeast(1)
        val newAmplitudes = FloatArray(barCount)
        var sumSquares = 0f

        for (i in 0 until barCount) {
            val idx = (i * step).coerceIn(0, waveform.size - 1)
            val raw = waveform[idx].toInt() and 0xFF
            val normalized = abs(raw - 128) / 128f
            val value = normalized.coerceIn(0.12f, 1.0f)
            
            sumSquares += value * value

            // Apply smoothing: react instantly to rise, decay slowly
            if (value > smoothedAmplitudes[i]) {
                smoothedAmplitudes[i] = value
            } else {
                smoothedAmplitudes[i] = smoothedAmplitudes[i] * decayFactor + value * (1f - decayFactor)
            }
            newAmplitudes[i] = smoothedAmplitudes[i]
        }

        _barAmplitudes.value = newAmplitudes
        val rms = sqrt(sumSquares / barCount)
        _audioLevel.value = (rms * 1.5f).coerceIn(0f, 1f)
    }

    fun release() {
        runCatching {
            visualizer?.enabled = false
            visualizer?.release()
            visualizer = null
            smoothedAmplitudes = FloatArray(64) { 0f }
            _barAmplitudes.value = FloatArray(64) { 0f }
            _audioLevel.value = 0f
        }
    }
}
