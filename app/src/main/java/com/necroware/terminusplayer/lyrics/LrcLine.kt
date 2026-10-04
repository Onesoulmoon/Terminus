package com.necroware.terminusplayer.lyrics

data class LyricWord(
    val text: String,
    val timestampMs: Long
)

data class LyricLine(
    val timestampMs: Long,
    val text: String,
    val words: List<LyricWord> = emptyList()
)
