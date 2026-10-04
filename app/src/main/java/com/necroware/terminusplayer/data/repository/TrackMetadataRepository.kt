package com.necroware.terminusplayer.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

data class TrackMetadata(
    val title: String,
    val artist: String,
    val artwork: Bitmap?,
    val durationMs: Long
)

@Singleton
class TrackMetadataRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun loadTrackMetadata(uri: Uri): TrackMetadata = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        var title = "UNKNOWN"
        var artist = "UNKNOWN"
        var durationMs = 0L
        var bitmap: Bitmap? = null

        try {
            retriever.setDataSource(context, uri)
            title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: "UNKNOWN"
            artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "UNKNOWN"
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            durationMs = durationStr?.toLongOrNull() ?: 0L
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            val rawArt = retriever.embeddedPicture
            if (rawArt != null) {
                bitmap = BitmapFactory.decodeByteArray(rawArt, 0, rawArt.size)
            }
        } catch (e: OutOfMemoryError) {
            bitmap = null
        } catch (e: Exception) {
            bitmap = null
        } finally {
            try {
                retriever.release()
            } catch (_: Exception) {}
        }

        TrackMetadata(title = title, artist = artist, artwork = bitmap, durationMs = durationMs)
    }
}
