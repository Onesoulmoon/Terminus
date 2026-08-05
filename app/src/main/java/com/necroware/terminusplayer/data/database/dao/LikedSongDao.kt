package com.necroware.terminusplayer.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.necroware.terminusplayer.data.database.entity.LikedSongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LikedSongDao {

    @Query("SELECT songId FROM liked_songs")
    fun observeLikedIds(): Flow<List<Long>>

    @Query("SELECT songId FROM liked_songs ORDER BY likedAt DESC")
    suspend fun getLikedIdsMostRecentFirst(): List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun like(entity: LikedSongEntity)

    @Query("DELETE FROM liked_songs WHERE songId = :songId")
    suspend fun unlike(songId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM liked_songs WHERE songId = :songId)")
    suspend fun isLiked(songId: Long): Boolean
}
