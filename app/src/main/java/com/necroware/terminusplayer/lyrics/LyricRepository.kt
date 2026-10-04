package com.necroware.terminusplayer.lyrics

object LyricRepository {

    suspend fun getLyrics(
        audioPath: String?,
        trackTitle: String,
        artistName: String
    ): LyricResult {
        // 1. Embedded ID3/FLAC Tag Reader
        val embedded = EmbeddedLyricsReader.extractEmbeddedLyrics(audioPath)
        if (embedded != null) {
            return LyricResult.Success(LrcParser.parse(embedded), "SRC // EMBEDDED_TAG")
        }

        // 2. Local Sidecar .lrc File Reader
        val localLrc = LocalLrcReader.findLocalLrc(audioPath)
        if (localLrc != null) {
            return LyricResult.Success(LrcParser.parse(localLrc), "SRC // LOCAL_LRC_FILE")
        }

        // 3. LRCLIB Network Fetcher (Exact + Fuzzy)
        val remoteLrc = LrclibFetcher.fetchSyncedLyrics(trackTitle, artistName)
        if (remoteLrc != null) {
            return LyricResult.Success(LrcParser.parse(remoteLrc), "SRC // LRCLIB_NET")
        }

        // 4. Fallback Failure
        return LyricResult.Error("ERR_404 // NO_LYRICS_FOUND_IN_INDEX")
    }
}

sealed class LyricResult {
    data class Success(val lines: List<LyricLine>, val sourceTag: String) : LyricResult()
    data class Error(val message: String) : LyricResult()
}
