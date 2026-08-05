package com.necroware.terminusplayer.util

import java.util.Locale

fun Long.toMinutesSeconds(): String {
    val totalSeconds = this / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%d:%02d", minutes, seconds)
}

/** For total listening-time durations (e.g. "2h 14m"), not song position. */
fun Long.toHoursMinutes(): String {
    val totalMinutes = this / 60000
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}
