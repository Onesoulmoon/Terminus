package com.necroware.terminusplayer.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.necroware.terminusplayer.data.database.entity.SongEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {

    @Query("SELECT * FROM songs ORDER BY title COLLATE NOCASE ASC")
    fun observeAllSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%' OR album LIKE '%' || :query || '%'")
    fun searchSongs(query: String): Flow<List<SongEntity>>

    @Query("SELECT DISTINCT artist FROM songs ORDER BY artist COLLATE NOCASE ASC")
    fun observeAllArtists(): Flow<List<String>>

    @Query("SELECT * FROM songs WHERE artist = :artist ORDER BY album, trackNumber ASC")
    fun observeSongsByArtist(artist: String): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE albumId = :albumId ORDER BY trackNumber ASC")
    fun observeSongsByAlbum(albumId: Long): Flow<List<SongEntity>>

    /**
     * Merges every MediaStore albumId that shares the same album title
     * (case-insensitive). MediaStore commonly assigns a distinct albumId
     * per track when per-track artist/feature tags differ, which otherwise
     * fragments one real album into many list entries.
     */
    @Query("SELECT * FROM songs WHERE album = :albumTitle COLLATE NOCASE ORDER BY trackNumber ASC")
    fun observeSongsByAlbumTitle(albumTitle: String): Flow<List<SongEntity>>

    @Query("SELECT DISTINCT folderPath FROM songs ORDER BY folderPath ASC")
    fun observeAllFolders(): Flow<List<String>>

    @Query("SELECT * FROM songs WHERE folderPath = :folderPath ORDER BY title ASC")
    fun observeSongsByFolder(folderPath: String): Flow<List<SongEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(songs: List<SongEntity>)

    @Query("DELETE FROM songs WHERE mediaStoreId NOT IN (:validIds)")
    suspend fun pruneDeleted(validIds: List<Long>)

    @Query("SELECT * FROM songs WHERE mediaStoreId = :id LIMIT 1")
    suspend fun getById(id: Long): SongEntity?

    @Query("SELECT * FROM songs WHERE mediaStoreId IN (:ids)")
    suspend fun getByIds(ids: List<Long>): List<SongEntity>

    @Query("SELECT * FROM songs ORDER BY dateAdded DESC LIMIT :limit")
    suspend fun mostRecentlyAdded(limit: Int): List<SongEntity>

    @Query("SELECT COUNT(*) FROM songs")
    suspend fun count(): Int
}
