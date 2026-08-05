package com.necroware.terminusplayer.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.necroware.terminusplayer.data.database.entity.PlaylistEntity
import com.necroware.terminusplayer.data.database.entity.PlaylistSongEntity
import com.necroware.terminusplayer.data.database.entity.SongEntity
import kotlinx.coroutines.flow.Flow

data class PlaylistWithCount(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val songCount: Int
)

@Dao
interface PlaylistDao {

    @Query(
        """
        SELECT playlists.id as id, playlists.name as name, playlists.createdAt as createdAt,
               COUNT(playlist_songs.songId) as songCount
        FROM playlists
        LEFT JOIN playlist_songs ON playlist_songs.playlistId = playlists.id
        GROUP BY playlists.id
        ORDER BY playlists.createdAt DESC
        """
    )
    fun observePlaylists(): Flow<List<PlaylistWithCount>>

    @Insert
    suspend fun insertPlaylist(playlist: PlaylistEntity): Long

    @Insert
    suspend fun insertPlaylistSongs(songs: List<PlaylistSongEntity>)

    @Query(
        """
        SELECT songs.* FROM songs
        INNER JOIN playlist_songs ON playlist_songs.songId = songs.mediaStoreId
        WHERE playlist_songs.playlistId = :playlistId
        ORDER BY playlist_songs.position ASC
        """
    )
    suspend fun getSongsForPlaylist(playlistId: Long): List<SongEntity>

    @Query("SELECT name FROM playlists WHERE id = :playlistId LIMIT 1")
    suspend fun getPlaylistName(playlistId: Long): String?

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId")
    suspend fun deletePlaylistSongs(playlistId: Long)
}
