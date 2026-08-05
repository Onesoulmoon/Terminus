package com.necroware.terminusplayer.util

import android.net.Uri
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.necroware.terminusplayer.data.model.Song

private const val EXTRA_ALBUM_ID = "com.necroware.terminusplayer.ALBUM_ID"
private const val EXTRA_SIZE_BYTES = "com.necroware.terminusplayer.SIZE_BYTES"

fun Song.toMediaItem(): MediaItem {
    val extras = Bundle().apply {
        putLong(EXTRA_ALBUM_ID, albumId)
        putLong(EXTRA_SIZE_BYTES, sizeBytes)
    }

    val metadata = MediaMetadata.Builder()
        .setTitle(title)
        .setArtist(artist)
        .setAlbumTitle(album)
        .setExtras(extras)
        .build()

    return MediaItem.Builder()
        .setMediaId(id.toString())
        .setUri(Uri.parse(uriString))
        .setMediaMetadata(metadata)
        .build()
}

fun List<Song>.toMediaItems(): List<MediaItem> = map { it.toMediaItem() }

/** Reads the albumId stashed in MediaMetadata.extras, or -1 if absent/not a Song-derived item. */
fun MediaMetadata.albumIdOrNull(): Long? = extras?.getLong(EXTRA_ALBUM_ID, -1L)?.takeIf { it != -1L }

/** Reads the file size (bytes) stashed in MediaMetadata.extras, or 0 if absent. */
fun MediaMetadata.sizeBytesOrZero(): Long = extras?.getLong(EXTRA_SIZE_BYTES, 0L) ?: 0L
