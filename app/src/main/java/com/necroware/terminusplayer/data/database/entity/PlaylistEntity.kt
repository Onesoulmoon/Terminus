package com.necroware.terminusplayer.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val createdAt: Long
)

/**
 * Composite-keyed join row rather than a List<Long> column — keeps the
 * songId FK queryable/indexable and lets [position] preserve the imported
 * playlist's original track order (Room doesn't guarantee list-column
 * ordering the way a real row-per-entry table does).
 */
@Entity(tableName = "playlist_songs", primaryKeys = ["playlistId", "songId"])
data class PlaylistSongEntity(
    val playlistId: Long,
    val songId: Long,
    val position: Int
)
