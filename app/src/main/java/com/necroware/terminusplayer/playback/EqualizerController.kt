package com.necroware.terminusplayer.playback

import android.media.audiofx.DynamicsProcessing
import android.media.audiofx.Equalizer
import android.os.Build
import android.util.Log
import com.necroware.terminusplayer.data.prefs.EqualizerSettings

/**
 * Hardware-accelerated audio effects controller bound to the player's audio session.
 * Uses DynamicsProcessing API (API 28+) for 10-band Parametric EQ, high-pass/low-pass
 * filtering, and pre-amp gain tuning, with fallback to standard Equalizer on older devices.
 */
class EqualizerController {

    private var dynamicsProcessing: DynamicsProcessing? = null
    private var legacyEqualizer: Equalizer? = null
    private var currentSessionId: Int = -1
    private var isEnabled: Boolean = false

    companion object {
        val EQ_CENTER_FREQS_HZ = floatArrayOf(
            31.25f, 62.5f, 125f, 250f, 500f,
            1000f, 2000f, 4000f, 8000f, 16000f
        )
    }

    fun attachToSession(audioSessionId: Int) {
        if (audioSessionId == currentSessionId && (dynamicsProcessing != null || legacyEqualizer != null)) return
        release()
        currentSessionId = audioSessionId

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            try {
                val bandCount = EQ_CENTER_FREQS_HZ.size
                val config = DynamicsProcessing.Config.Builder(
                    DynamicsProcessing.VARIANT_FAVOR_FREQUENCY_RESOLUTION,
                    2, // Stereo channel count
                    true, bandCount, // PreEQ enabled with 10 bands
                    false, 0,        // MultiBandCompressor disabled
                    false, 0,        // PostEQ disabled
                    false            // Limiter disabled
                ).build()

                dynamicsProcessing = DynamicsProcessing(0, audioSessionId, config).apply {
                    enabled = isEnabled
                }

                // Configure EQ band frequencies
                val dp = dynamicsProcessing
                if (dp != null) {
                    for (i in EQ_CENTER_FREQS_HZ.indices) {
                        val band = DynamicsProcessing.EqBand(true, EQ_CENTER_FREQS_HZ[i], 0f)
                        dp.setPreEqBandAllChannelsTo(i, band)
                    }
                }
                Log.i("EqualizerController", "Attached DynamicsProcessing PEQ to session $audioSessionId")
                return
            } catch (e: Exception) {
                Log.w("EqualizerController", "DynamicsProcessing unavailable, falling back to legacy Equalizer", e)
                dynamicsProcessing = null
            }
        }

        // Fallback for pre-API 28 or unsupported hardware
        legacyEqualizer = runCatching {
            Equalizer(0, audioSessionId).apply { setEnabled(isEnabled) }
        }.getOrElse {
            Log.w("EqualizerController", "Could not attach legacy Equalizer to session $audioSessionId", it)
            null
        }
    }

    fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
        runCatching { dynamicsProcessing?.enabled = enabled }
        runCatching { legacyEqualizer?.setEnabled(enabled) }
    }

    fun applyEqualizerSettings(settings: EqualizerSettings) {
        setEnabled(settings.enabled)
        val dp = dynamicsProcessing
        if (dp != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            runCatching {
                val preamp = settings.preampGainDb
                val hpEnabled = settings.highPassEnabled
                val hpFreq = settings.highPassFreq
                val lpEnabled = settings.lowPassEnabled
                val lpFreq = settings.lowPassFreq

                val bandCount = EQ_CENTER_FREQS_HZ.size
                for (i in 0 until bandCount) {
                    val centerFreq = EQ_CENTER_FREQS_HZ[i]
                    var gainDb = settings.bandGainsDb.getOrElse(i) { 0 }.toFloat() + preamp

                    // High-pass filter roll-off simulation
                    if (hpEnabled && centerFreq < hpFreq) {
                        val ratio = (centerFreq / hpFreq).coerceIn(0.01f, 1f)
                        gainDb -= (1f - ratio) * 24f
                    }

                    // Low-pass filter roll-off simulation
                    if (lpEnabled && centerFreq > lpFreq) {
                        val ratio = (lpFreq / centerFreq).coerceIn(0.01f, 1f)
                        gainDb -= (1f - ratio) * 24f
                    }

                    val band = DynamicsProcessing.EqBand(true, centerFreq, gainDb.coerceIn(-24f, 24f))
                    dp.setPreEqBandAllChannelsTo(i, band)
                }
            }.onFailure { Log.e("EqualizerController", "Failed applying DynamicsProcessing settings", it) }
        } else {
            applyBandGains(settings.bandGainsDb)
        }
    }

    /** Legacy 5-band gain application */
    fun applyBandGains(gainsDb: List<Int>) {
        val eq = legacyEqualizer ?: return
        runCatching {
            val range = eq.bandLevelRange
            val minMilliBel = range[0]
            val maxMilliBel = range[1]
            val bandCount = eq.numberOfBands.toInt()
            for (band in 0 until bandCount) {
                val gainDb = gainsDb.getOrElse(band) { 0 }
                val milliBel = (gainDb * 100).coerceIn(minMilliBel.toInt(), maxMilliBel.toInt())
                eq.setBandLevel(band.toShort(), milliBel.toShort())
            }
        }
    }

    fun release() {
        runCatching { dynamicsProcessing?.release() }
        runCatching { legacyEqualizer?.release() }
        dynamicsProcessing = null
        legacyEqualizer = null
        currentSessionId = -1
    }
}
