package com.necroware.terminusplayer.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val mediaStoreId: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val duration: Long,
    val uriString: String,
    val dateAdded: Long,
    val trackNumber: Int = 0,
    val year: Int = 0,
    val folderPath: String = "",
    val sizeBytes: Long = 0L
)
