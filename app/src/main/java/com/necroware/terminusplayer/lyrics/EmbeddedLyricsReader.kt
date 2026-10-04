package com.necroware.terminusplayer.lyrics

import android.media.MediaMetadataRetriever
import android.os.Build

object EmbeddedLyricsReader {

    fun extractEmbeddedLyrics(filePath: String?): String? {
        if (filePath.isNullOrEmpty()) return null

        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(filePath)
            // METADATA_KEY_LYRIC extracts ID3 USLT / FLAC LYRICS frames (API 31+)
            val rawLyrics = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                retriever.extractMetadata(32) // METADATA_KEY_LYRIC
            } else {
                null
            }
            if (!rawLyrics.isNullAndBlank()) rawLyrics else null
        } catch (e: Exception) {
            null
        } finally {
            runCatching { retriever.release() }
        }
    }

    private fun String?.isNullAndBlank(): Boolean = this == null || this.trim().isEmpty()
}
