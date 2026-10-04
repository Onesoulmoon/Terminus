package com.necroware.terminusplayer.lyrics

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

object LrclibFetcher {

    // LRCLIB REQUIRES a distinct custom User-Agent
    private const val USER_AGENT = "TerminusPlayer/1.8.0 (https://github.com/necroware/terminusplayer)"

    suspend fun fetchSyncedLyrics(trackName: String, artistName: String): String? = withContext(Dispatchers.IO) {
        val cleanTrack = LyricSanitizer.cleanTitle(trackName)
        val cleanArtist = LyricSanitizer.cleanArtist(artistName)

        // Stage 1: Try Direct Exact Match GET
        val exactResult = tryGetExact(cleanTrack, cleanArtist)
        if (exactResult != null) return@withContext exactResult

        // Stage 2: Fallback to Fuzzy Query Search
        return@withContext tryFuzzySearch("$cleanTrack $cleanArtist")
    }

    private fun tryGetExact(track: String, artist: String): String? {
        return runCatching {
            val encodedTrack = URLEncoder.encode(track, "UTF-8")
            val encodedArtist = URLEncoder.encode(artist, "UTF-8")
            val urlStr = "https://lrclib.net/api/get?track_name=$encodedTrack&artist_name=$encodedArtist"

            val connection = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("User-Agent", USER_AGENT)
                connectTimeout = 5000
                readTimeout = 5000
            }

            if (connection.responseCode == 200) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseText)
                parseLyricsFromJsonObject(json)
            } else null
        }.getOrNull()
    }

    private fun tryFuzzySearch(query: String): String? {
        return runCatching {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val urlStr = "https://lrclib.net/api/search?q=$encodedQuery"

            val connection = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("User-Agent", USER_AGENT)
                connectTimeout = 5000
                readTimeout = 5000
            }

            if (connection.responseCode == 200) {
                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                val jsonArray = JSONArray(responseText)

                // Find first result in array that contains valid synced or plain lyrics
                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(i)
                    val lyrics = parseLyricsFromJsonObject(item)
                    if (lyrics != null) return lyrics
                }
            }
            null
        }.getOrNull()
    }

    private fun parseLyricsFromJsonObject(json: JSONObject): String? {
        val synced = json.optString("syncedLyrics", "").takeIf { it.isNotBlank() }
        val plain = json.optString("plainLyrics", "").takeIf { it.isNotBlank() }
        return synced ?: plain
    }
}
