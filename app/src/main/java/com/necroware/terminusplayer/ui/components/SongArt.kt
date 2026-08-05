package com.necroware.terminusplayer.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.LruCache
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Loads album art per-FILE via ContentResolver.loadThumbnail (API 29+),
 * replacing the earlier approach of looking up the legacy shared
 * `content://media/external/audio/albumart/{albumId}` table by numeric
 * albumId.
 *
 * That legacy table is keyed by a shared numeric ID that Android can
 * (and does, in practice) leave mapped to stale or reused artwork —
 * this is the confirmed cause of one song's art rendering as a totally
 * different song's cover. Reading the thumbnail directly from each
 * song's own content URI ties the art to that exact file, so it can't
 * cross-contaminate between songs/albums.
 */
@Composable
fun SongArt(
    uriString: String,
    modifier: Modifier = Modifier,
    size: Dp = 56.dp
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val sizePx = with(density) { size.roundToPx() }.coerceAtLeast(1)
    // Cache key includes the requested pixel size — without this, a
    // low-res thumbnail cached from a small view (mini player, library
    // row) would get reused and stretched for a much bigger view (Now
    // Playing), which is exactly what was causing the blurriness.
    val cacheKey = "$uriString@$sizePx"

    var bitmap by remember(cacheKey) { mutableStateOf(SongArtCache.get(cacheKey)) }

    LaunchedEffect(cacheKey) {
        if (uriString.isBlank() || bitmap != null) return@LaunchedEffect
        val loaded = loadThumbnailSafely(context, uriString, sizePx)
        if (loaded != null) {
            SongArtCache.put(cacheKey, loaded)
            bitmap = loaded
        }
    }

    Box(modifier = modifier.size(size)) {
        val current = bitmap
        if (current != null) {
            Image(
                bitmap = current.asImageBitmap(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(size)
            )
        } else {
            SongArtFallback(size)
        }
    }
}

@Composable
private fun SongArtFallback(size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "♪",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

private suspend fun loadThumbnailSafely(context: Context, uriString: String, sizePx: Int): Bitmap? =
    withContext(Dispatchers.IO) {
        try {
            val uri = Uri.parse(uriString)
            context.contentResolver.loadThumbnail(uri, android.util.Size(sizePx, sizePx), null)
        } catch (_: Exception) {
            null
        }
    }

/** Small in-memory LRU cache keyed by content URI, capped by entry count (not byte size, for simplicity). */
private object SongArtCache {
    private val cache = LruCache<String, Bitmap>(120)
    fun get(key: String): Bitmap? = cache.get(key)
    fun put(key: String, bitmap: Bitmap) = cache.put(key, bitmap)
}
