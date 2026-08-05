package com.necroware.terminusplayer.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.necroware.terminusplayer.data.database.dao.LikedSongDao
import com.necroware.terminusplayer.data.database.dao.PlayEventDao
import com.necroware.terminusplayer.data.database.dao.PlaylistDao
import com.necroware.terminusplayer.data.database.dao.SongDao
import com.necroware.terminusplayer.data.database.entity.LikedSongEntity
import com.necroware.terminusplayer.data.database.entity.PlayEventEntity
import com.necroware.terminusplayer.data.database.entity.PlaylistEntity
import com.necroware.terminusplayer.data.database.entity.PlaylistSongEntity
import com.necroware.terminusplayer.data.database.entity.SongEntity

@Database(
    entities = [
        SongEntity::class,
        LikedSongEntity::class,
        PlayEventEntity::class,
        PlaylistEntity::class,
        PlaylistSongEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class TerminusDatabase : RoomDatabase() {
    abstract fun songDao(): SongDao
    abstract fun likedSongDao(): LikedSongDao
    abstract fun playEventDao(): PlayEventDao
    abstract fun playlistDao(): PlaylistDao

    companion object {
        const val DATABASE_NAME = "terminus.db"
    }
}
