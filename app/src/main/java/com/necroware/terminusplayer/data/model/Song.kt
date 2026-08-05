package com.necroware.terminusplayer.data.model

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val duration: Long,
    val uriString: String,
    val trackNumber: Int = 0,
    val year: Int = 0,
    val folderPath: String = "",
    val sizeBytes: Long = 0L,
    val dateAdded: Long = 0L,
    val isLiked: Boolean = false
)

data class Album(
    val id: Long,
    val title: String,
    val artist: String,
    val songCount: Int,
    /** URI of one representative song in this (title-merged) album, used
     *  for art lookup — per-file art is reliable; the shared numeric
     *  albumId legacy lookup is not (see SongArt.kt). */
    val representativeUriString: String = ""
)

data class Artist(
    val name: String,
    val songCount: Int
)

data class Folder(
    val path: String,
    val name: String,
    val songCount: Int
)

data class Playlist(
    val id: Long,
    val name: String,
    val songCount: Int
)
