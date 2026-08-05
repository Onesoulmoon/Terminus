package com.necroware.terminusplayer.util

import com.necroware.terminusplayer.data.model.Song
import java.io.BufferedReader

/**
 * Parses an M3U/M3U8 playlist into the raw entry strings (file paths or
 * URLs, one per non-comment line) — this is deliberately just the file's
 * own text, not yet resolved against anything on-device.
 */
fun parseM3u(reader: BufferedReader): List<String> =
    reader.lineSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith("#") }
        .toList()

/**
 * Best-effort match of an M3U entry (usually an absolute path from
 * whatever device/app exported the playlist, which essentially never
 * matches this device's paths 1:1) against the scanned library by
 * comparing normalized file basenames to song titles. Playlists are
 * portable text files with no stable ID to match on, so this is
 * inherently fuzzy — exact hits first, then a loose contains-match.
 */
fun matchM3uEntryToSong(entry: String, songsByNormalizedTitle: Map<String, List<Song>>): Song? {
    val basename = entry.substringAfterLast('/').substringAfterLast('\\')
        .substringBeforeLast('.')
    val normalized = normalizeForMatch(basename)
    if (normalized.isBlank()) return null

    songsByNormalizedTitle[normalized]?.firstOrNull()?.let { return it }

    return songsByNormalizedTitle.entries
        .firstOrNull { (key, _) -> key.contains(normalized) || normalized.contains(key) }
        ?.value?.firstOrNull()
}

fun normalizeForMatch(raw: String): String =
    raw.lowercase()
        .replace('_', ' ')
        .replace('-', ' ')
        .replace(Regex("\\s+"), " ")
        .trim()
