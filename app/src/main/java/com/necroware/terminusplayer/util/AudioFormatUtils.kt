package com.necroware.terminusplayer.util

import androidx.media3.common.C
import androidx.media3.common.Format

private val LOSSLESS_MIME_HINTS = listOf("flac", "wav", "x-adpcm", "alac", "raw")

/** e.g. "FLAC 96kHz", "MP3 44kHz", or "—" if nothing is resolvable yet. */
fun Format.toAudioFormatLabel(): String {
    val khz = if (sampleRate > 0) "${sampleRate / 1000}kHz" else null
    return listOfNotNull(toCodecLabel(), khz).joinToString(" ").ifBlank { "—" }
}

/** e.g. "MP3", "FLAC" — just the codec, no sample rate. */
fun Format.toCodecLabel(): String {
    val mime = sampleMimeType?.lowercase().orEmpty()
    return when {
        mime.contains("flac") -> "FLAC"
        mime.contains("wav") || mime.contains("x-adpcm") || mime.contains("raw") -> "WAV"
        mime.contains("alac") -> "ALAC"
        mime.contains("mpeg") || mime.contains("mp3") -> "MP3"
        mime.contains("aac") -> "AAC"
        mime.contains("opus") -> "OPUS"
        mime.contains("vorbis") || mime.contains("ogg") -> "OGG"
        mime.isNotBlank() -> mime.substringAfterLast('/').uppercase()
        else -> "—"
    }
}

/** e.g. "44.1 kHz" — just the sample rate, no codec. */
fun Format.toSampleRateLabel(): String =
    if (sampleRate > 0) "%.1f kHz".format(sampleRate / 1000f) else "—"

fun Format.isLosslessFormat(): Boolean {
    val mime = sampleMimeType?.lowercase().orEmpty()
    return LOSSLESS_MIME_HINTS.any { mime.contains(it) }
}

/**
 * e.g. "16" or "24". Format.pcmEncoding is only actually populated for raw
 * PCM sources (WAV) — for every compressed codec (MP3/AAC/OPUS/most FLAC as
 * seen here) Android's decoders output 16-bit PCM regardless of source
 * depth, so that's what's reported rather than a source bit depth we have
 * no reliable way to read from a compressed Format.
 */
fun Format.toBitDepthLabel(): String = when (pcmEncoding) {
    C.ENCODING_PCM_32BIT, C.ENCODING_PCM_32BIT_BIG_ENDIAN -> "32"
    C.ENCODING_PCM_24BIT, C.ENCODING_PCM_24BIT_BIG_ENDIAN -> "24"
    C.ENCODING_PCM_16BIT, C.ENCODING_PCM_16BIT_BIG_ENDIAN -> "16"
    else -> "16"
}

/** Average bitrate in kbps from the container's own Format field, or -1 if unknown. */
fun Format.bitrateKbpsOrNegative(): Int = if (bitrate != Format.NO_VALUE && bitrate > 0) bitrate / 1000 else -1

/**
 * Falls back to size/duration when the container didn't expose a bitrate
 * (common for some FLAC streams) — same arithmetic any player uses to
 * estimate it: bytes * 8 / seconds.
 */
fun estimateKbpsFromSize(sizeBytes: Long, durationMs: Long): Int {
    if (sizeBytes <= 0L || durationMs <= 0L) return -1
    val seconds = durationMs / 1000.0
    if (seconds <= 0.0) return -1
    return ((sizeBytes * 8L) / seconds / 1000.0).toInt()
}

/** e.g. "5.7mb", or "—" if unknown. */
fun sizeLabelFromBytes(sizeBytes: Long): String {
    if (sizeBytes <= 0L) return "—"
    val mb = sizeBytes / (1024.0 * 1024.0)
    return "%.1fmb".format(mb)
}

/** Selected audio track's Format from the current Tracks, if any. */
fun androidx.media3.common.Tracks.selectedAudioFormat(): Format? {
    for (group in groups) {
        if (group.type != C.TRACK_TYPE_AUDIO) continue
        for (i in 0 until group.length) {
            if (group.isTrackSelected(i)) return group.getTrackFormat(i)
        }
    }
    return null
}
