package com.necroware.terminusplayer.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "liked_songs")
data class LikedSongEntity(
    @PrimaryKey val songId: Long,
    val likedAt: Long
)
