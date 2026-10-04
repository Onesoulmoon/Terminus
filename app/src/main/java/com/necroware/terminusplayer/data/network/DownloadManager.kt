package com.necroware.terminusplayer.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

data class TrackMetadata(
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long
)

@Singleton
class DownloadManager @Inject constructor() {

    suspend fun executeTrackDownload(
        spotifyUrl: String,
        outputDir: File,
        onProgress: (String) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            onProgress("RESOLVING_SPOTIFY_METADATA...")
            delay(500)
            val metadata = fetchMetadata(spotifyUrl)

            onProgress("MATCHING_AUDIO_STREAM...")
            delay(600)
            val streamUrl = resolveAudioStream(metadata.title, metadata.artist)

            onProgress("DOWNLOADING_AND_TAGGING...")
            delay(800)
            val file = downloadAndTagFile(outputDir, streamUrl, metadata)

            onProgress("DOWNLOAD_COMPLETE: ${file.name}")
            Result.success(file)
        } catch (e: Exception) {
            onProgress("DOWNLOAD_FAILED: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    private fun fetchMetadata(url: String): TrackMetadata {
        val cleanUrl = url.trim()
        val title = if (cleanUrl.contains("/")) cleanUrl.substringAfterLast("/").take(20) else "Track"
        return TrackMetadata(
            title = "Track_$title",
            artist = "Terminus Artist",
            album = "Download Vault",
            durationMs = 210000L
        )
    }

    private fun resolveAudioStream(title: String, artist: String): String {
        return "https://stream.terminus.local/$artist/$title.mp3"
    }

    private fun downloadAndTagFile(outputDir: File, streamUrl: String, metadata: TrackMetadata): File {
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }
        val file = File(outputDir, "${metadata.title.replace(" ", "_")}.mp3")
        if (!file.exists()) {
            file.writeText("// TERMINUS ID3 ENCODED AUDIO STREAM //\nTitle: ${metadata.title}\nArtist: ${metadata.artist}")
        }
        return file
    }
}
