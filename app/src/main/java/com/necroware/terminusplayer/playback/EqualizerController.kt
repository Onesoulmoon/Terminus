package com.necroware.terminusplayer.playback

import android.media.audiofx.Equalizer
import android.util.Log

/**
 * Wraps a real android.media.audiofx.Equalizer bound to the player's audio
 * session. Re-created whenever the session id changes (ExoPlayer assigns a
 * new one occasionally, e.g. after certain track transitions) since an
 * Equalizer instance is tied to the session it was constructed with.
 * Android's Equalizer always exposes 5 bands on essentially every real
 * device, which is why the app's EqualizerSettings is fixed at 5 bands
 * rather than reading getNumberOfBands() — keeps Settings' band count
 * static and predictable instead of varying by device.
 */
class EqualizerController {

    private var equalizer: Equalizer? = null
    private var currentSessionId: Int = -1

    fun attachToSession(audioSessionId: Int) {
        if (audioSessionId == currentSessionId && equalizer != null) return
        release()
        currentSessionId = audioSessionId
        equalizer = runCatching {
            Equalizer(0, audioSessionId).apply { setEnabled(false) }
        }.getOrElse {
            Log.w("EqualizerController", "Could not attach Equalizer to session $audioSessionId", it)
            null
        }
    }

    fun setEnabled(enabled: Boolean) {
        runCatching { equalizer?.setEnabled(enabled) }
    }

    /** gainsDb: one entry per band, in dB, clamped to the device's supported range. */
    fun applyBandGains(gainsDb: List<Int>) {
        val eq = equalizer ?: return
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
        runCatching { equalizer?.release() }
        equalizer = null
    }
}
