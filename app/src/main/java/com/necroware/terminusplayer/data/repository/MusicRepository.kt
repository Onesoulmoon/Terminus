package com.necroware.terminusplayer.data.repository

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.necroware.terminusplayer.data.database.dao.LikedSongDao
import com.necroware.terminusplayer.data.database.dao.PlayEventDao
import com.necroware.terminusplayer.data.database.dao.PlaylistDao
import com.necroware.terminusplayer.data.database.dao.SongDao
import com.necroware.terminusplayer.data.database.entity.LikedSongEntity
import com.necroware.terminusplayer.data.database.entity.PlaylistEntity
import com.necroware.terminusplayer.data.database.entity.PlaylistSongEntity
import com.necroware.terminusplayer.data.database.entity.SongEntity
import com.necroware.terminusplayer.data.mediastore.MediaStoreScanner
import com.necroware.terminusplayer.data.model.Album
import com.necroware.terminusplayer.data.model.Artist
import com.necroware.terminusplayer.data.model.Playlist
import com.necroware.terminusplayer.data.model.Song
import com.necroware.terminusplayer.util.matchM3uEntryToSong
import com.necroware.terminusplayer.util.normalizeForMatch
import com.necroware.terminusplayer.util.parseM3u
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val songDao: SongDao,
    private val likedSongDao: LikedSongDao,
    private val playEventDao: PlayEventDao,
    private val playlistDao: PlaylistDao,
    private val scanner: MediaStoreScanner
) {

    /** Re-scans MediaStore and syncs the Room cache. Call on app start and pull-to-refresh. */
    suspend fun syncLibrary() {
        val scanned = scanner.scanAudioFiles()
        songDao.upsertAll(scanned)
        songDao.pruneDeleted(scanned.map { it.mediaStoreId })
    }

    fun observeAllSongs(): Flow<List<Song>> =
        combine(songDao.observeAllSongs(), likedSongDao.observeLikedIds()) { songs, likedIds ->
            val likedSet = likedIds.toHashSet()
            songs.map { it.toSong(isLiked = it.mediaStoreId in likedSet) }
        }

    fun observeAllArtists(): Flow<List<Artist>> =
        combine(songDao.observeAllArtists(), songDao.observeAllSongs()) { artists, songs ->
            artists.map { name ->
                Artist(name = name, songCount = songs.count { it.artist == name })
            }
        }

    fun observeAllAlbums(): Flow<List<Album>> =
        songDao.observeAllSongs().map { songs ->
            // Group by normalized title, NOT raw albumId — MediaStore assigns
            // a distinct albumId per track when per-track artist/feature tags
            // differ, which otherwise splits one real album into many rows.
            songs.groupBy { it.album.trim().lowercase() }
                .map { (_, songsInAlbum) ->
                    val representative = songsInAlbum.first()
                    Album(
                        id = representative.albumId,
                        title = representative.album,
                        artist = representative.artist,
                        songCount = songsInAlbum.size,
                        representativeUriString = representative.uriString
                    )
                }
                .sortedBy { it.title.lowercase() }
        }

    fun observeAllFolders(): Flow<List<String>> = songDao.observeAllFolders()

    fun searchSongs(query: String): Flow<List<Song>> =
        songDao.searchSongs(query).map { entities -> entities.map { it.toSong() } }

    suspend fun toggleLike(songId: Long) {
        if (likedSongDao.isLiked(songId)) {
            likedSongDao.unlike(songId)
        } else {
            likedSongDao.like(LikedSongEntity(songId, System.currentTimeMillis()))
        }
    }

    /** Used by the Now Playing like-button to reflect the current track's liked state. */
    fun observeLikedIds(): Flow<List<Long>> = likedSongDao.observeLikedIds()

    /**
     * Media3's MediaController doesn't preserve a MediaItem's local content
     * URI across the session boundary (MediaItem.LocalConfiguration is
     * intentionally dropped during Bundle serialization), so Now Playing
     * resolves the current track's real file URI via this lookup instead
     * of trying to read it back off the controller.
     */
    suspend fun getSongUri(songId: Long): String? = songDao.getById(songId)?.uriString

    suspend fun getSongsForAlbum(albumTitle: String): List<Song> {
        val likedIds = likedSongDao.observeLikedIds().first().toHashSet()
        return songDao.observeSongsByAlbumTitle(albumTitle).first().map { it.toSong(isLiked = it.mediaStoreId in likedIds) }
    }

    suspend fun getSongsForArtist(artist: String): List<Song> {
        val likedIds = likedSongDao.observeLikedIds().first().toHashSet()
        return songDao.observeSongsByArtist(artist).first().map { it.toSong(isLiked = it.mediaStoreId in likedIds) }
    }

    /** Liked songs, most recently liked first — the "Liked Songs" auto-playlist. */
    suspend fun getLikedSongs(): List<Song> {
        val likedIdsOrdered = likedSongDao.getLikedIdsMostRecentFirst()
        if (likedIdsOrdered.isEmpty()) return emptyList()
        val entitiesById = songDao.getByIds(likedIdsOrdered).associateBy { it.mediaStoreId }
        return likedIdsOrdered.mapNotNull { id -> entitiesById[id]?.toSong(isLiked = true) }
    }

    /** All-time most-played songs, highest play count first — the "Most Played" auto-playlist. */
    suspend fun getMostPlayed(limit: Int = 100): List<Song> {
        val rows = playEventDao.topPlayedSongIds(limit)
        if (rows.isEmpty()) return emptyList()
        val likedIds = likedSongDao.observeLikedIds().first().toHashSet()
        val entitiesById = songDao.getByIds(rows.map { it.songId }).associateBy { it.mediaStoreId }
        return rows.mapNotNull { row ->
            entitiesById[row.songId]?.toSong(isLiked = row.songId in likedIds)
        }
    }

    /** Most-recently-listened-to distinct songs, most recent first, for the Home screen row. */
    suspend fun getRecentlyPlayed(limit: Int = 20): List<Song> {
        val rows = playEventDao.recentlyPlayedSongIds(limit)
        if (rows.isEmpty()) return emptyList()
        val likedIds = likedSongDao.observeLikedIds().first().toHashSet()
        val entitiesById = songDao.getByIds(rows.map { it.songId }).associateBy { it.mediaStoreId }
        // Room's IN clause doesn't preserve order, so re-order to match recency.
        return rows.mapNotNull { row ->
            entitiesById[row.songId]?.toSong(isLiked = row.songId in likedIds)
        }
    }

    /**
     * "Your Mix" — a lightweight algorithmic mix built from real listening
     * signal rather than a static shuffle:
     *  - Liked songs are always eligible.
     *  - Top-played songs are weighted into the pool proportional to how
     *    often they've been played (capped so one song can't dominate).
     *  - If there isn't enough listening history yet (new install), falls
     *    back to the most recently added songs so the screen never renders
     *    empty on day one.
     * Final pool is shuffled and trimmed to [limit] distinct songs.
     */
    suspend fun getYourMix(limit: Int = 25): List<Song> {
        val likedIds = likedSongDao.observeLikedIds().first()
        val topPlayed = playEventDao.topPlayedSongIds(limit = 50)

        val weightedBag = mutableListOf<Long>()
        topPlayed.forEach { row ->
            repeat(row.playCount.coerceIn(1, 5)) { weightedBag += row.songId }
        }
        likedIds.forEach { id ->
            repeat(3) { weightedBag += id }
        }

        val candidateIds: List<Long> = if (weightedBag.isEmpty()) {
            songDao.mostRecentlyAdded(limit * 2).map { it.mediaStoreId }
        } else {
            weightedBag.shuffled()
        }

        val distinctOrdered = candidateIds.distinct().take(limit)
        if (distinctOrdered.isEmpty()) return emptyList()

        val likedSet = likedIds.toHashSet()
        val entitiesById = songDao.getByIds(distinctOrdered).associateBy { it.mediaStoreId }
        return distinctOrdered
            .mapNotNull { id -> entitiesById[id]?.toSong(isLiked = id in likedSet) }
            .shuffled()
    }

    // ---- Custom playlists ------------------------------------------------

    fun observeCustomPlaylists(): Flow<List<Playlist>> =
        playlistDao.observePlaylists().map { rows ->
            rows.map { Playlist(id = it.id, name = it.name, songCount = it.songCount) }
        }

    suspend fun getSongsForPlaylist(playlistId: Long): List<Song> {
        val likedIds = likedSongDao.observeLikedIds().first().toHashSet()
        return playlistDao.getSongsForPlaylist(playlistId).map { it.toSong(isLiked = it.mediaStoreId in likedIds) }
    }

    suspend fun getPlaylistName(playlistId: Long): String = playlistDao.getPlaylistName(playlistId) ?: "PLAYLIST"

    suspend fun deletePlaylist(playlistId: Long) {
        playlistDao.deletePlaylistSongs(playlistId)
        playlistDao.deletePlaylist(playlistId)
    }

    /**
     * Reads an .m3u/.m3u8 file from [uri], matches each entry against the
     * already-scanned library by normalized filename, and stores whatever
     * matched as a new named playlist. Returns the number of tracks that
     * matched (out of the total entries found in the file) so the caller
     * can tell the user "$matched / $total tracks found" rather than a
     * silent partial import.
     */
    suspend fun importPlaylistFromM3u(uri: Uri, name: String): Pair<Int, Int> = withContext(Dispatchers.IO) {
        val entries = context.contentResolver.openInputStream(uri)?.use { stream ->
            parseM3u(stream.bufferedReader())
        } ?: emptyList()
        if (entries.isEmpty()) return@withContext 0 to 0

        val allSongs = songDao.observeAllSongs().first().map { it.toSong() }
        val byNormalizedTitle = allSongs.groupBy { normalizeForMatch(it.title) }

        val matched = entries.mapNotNull { entry -> matchM3uEntryToSong(entry, byNormalizedTitle) }
        if (matched.isEmpty()) return@withContext 0 to entries.size

        val playlistId = playlistDao.insertPlaylist(
            PlaylistEntity(name = name, createdAt = System.currentTimeMillis())
        )
        playlistDao.insertPlaylistSongs(
            matched.mapIndexed { index, song -> PlaylistSongEntity(playlistId, song.id, index) }
        )
        matched.size to entries.size
    }

    // ---- Add files ---------------------------------------------------

    /**
     * Copies each picked document into the shared Music/Terminus MediaStore
     * collection (scoped storage — no WRITE_EXTERNAL_STORAGE needed on
     * API 29+ since the app owns what it inserts) and re-syncs the library
     * so the new tracks show up immediately. Returns how many succeeded.
     */
    suspend fun importAudioFiles(uris: List<Uri>): Int = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        var successCount = 0

        for (sourceUri in uris) {
            val displayName = queryDisplayName(sourceUri) ?: continue
            val values = ContentValues().apply {
                put(MediaStore.Audio.Media.DISPLAY_NAME, displayName)
                put(MediaStore.Audio.Media.IS_MUSIC, 1)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_MUSIC + "/Terminus")
                    put(MediaStore.Audio.Media.IS_PENDING, 1)
                }
            }
            val destUri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values) ?: continue

            val copied = try {
                resolver.openInputStream(sourceUri)?.use { input ->
                    resolver.openOutputStream(destUri)?.use { output -> input.copyTo(output) }
                } != null
            } catch (e: Exception) {
                false
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val clearPending = ContentValues().apply { put(MediaStore.Audio.Media.IS_PENDING, 0) }
                resolver.update(destUri, clearPending, null, null)
            }

            if (copied) successCount++ else resolver.delete(destUri, null, null)
        }

        if (successCount > 0) syncLibrary()
        successCount
    }

    private fun queryDisplayName(uri: Uri): String? {
        val projection = arrayOf(android.provider.OpenableColumns.DISPLAY_NAME)
        context.contentResolver.query(uri, projection, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idx = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) return cursor.getString(idx)
            }
        }
        return uri.lastPathSegment
    }
}

private fun SongEntity.toSong(isLiked: Boolean = false): Song = Song(
    id = mediaStoreId,
    title = title,
    artist = artist,
    album = album,
    albumId = albumId,
    duration = duration,
    uriString = uriString,
    trackNumber = trackNumber,
    year = year,
    folderPath = folderPath,
    sizeBytes = sizeBytes,
    dateAdded = dateAdded,
    isLiked = isLiked
)
