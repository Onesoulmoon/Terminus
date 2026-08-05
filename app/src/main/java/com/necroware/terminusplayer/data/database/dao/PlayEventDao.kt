package com.necroware.terminusplayer.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.necroware.terminusplayer.data.database.entity.PlayEventEntity
import kotlinx.coroutines.flow.Flow

data class ArtistPlayCount(val artist: String, val playCount: Int, val msPlayed: Long)
data class DayPlayCount(val dayEpoch: Long, val playCount: Int, val msPlayed: Long)
data class HourHistogramRow(val hourOfDay: Int, val playCount: Int)

@Dao
interface PlayEventDao {

    @Insert
    suspend fun insert(event: PlayEventEntity)

    @Query("SELECT * FROM play_events WHERE startedAtEpochMs >= :sinceEpochMs ORDER BY startedAtEpochMs ASC")
    fun observeEventsSince(sinceEpochMs: Long): Flow<List<PlayEventEntity>>

    @Query(
        """
        SELECT artist, COUNT(*) as playCount, SUM(msPlayed) as msPlayed
        FROM play_events
        WHERE startedAtEpochMs >= :sinceEpochMs
        GROUP BY artist
        ORDER BY playCount DESC
        LIMIT :limit
        """
    )
    suspend fun topArtists(sinceEpochMs: Long, limit: Int = 10): List<ArtistPlayCount>

    @Query(
        """
        SELECT (startedAtEpochMs / 86400000) * 86400000 as dayEpoch,
               COUNT(*) as playCount,
               SUM(msPlayed) as msPlayed
        FROM play_events
        WHERE startedAtEpochMs >= :sinceEpochMs
        GROUP BY dayEpoch
        ORDER BY dayEpoch ASC
        """
    )
    suspend fun playsByDay(sinceEpochMs: Long): List<DayPlayCount>

    @Query("SELECT COUNT(*) FROM play_events WHERE startedAtEpochMs >= :sinceEpochMs")
    suspend fun totalPlays(sinceEpochMs: Long): Int

    @Query("SELECT COALESCE(SUM(msPlayed), 0) FROM play_events WHERE startedAtEpochMs >= :sinceEpochMs")
    suspend fun totalMsPlayed(sinceEpochMs: Long): Long

    @Query(
        """
        SELECT CAST(strftime('%H', startedAtEpochMs / 1000, 'unixepoch', 'localtime') AS INTEGER) as hourOfDay,
               COUNT(*) as playCount
        FROM play_events
        WHERE startedAtEpochMs >= :sinceEpochMs
        GROUP BY hourOfDay
        ORDER BY hourOfDay ASC
        """
    )
    suspend fun playsByHourOfDay(sinceEpochMs: Long): List<HourHistogramRow>

    /**
     * Most-recently-started distinct songs, most recent first. Backs the
     * Home screen "Recently Played" row. MAX(startedAtEpochMs) per songId
     * dedupes repeat listens down to one row per song.
     */
    @Query(
        """
        SELECT songId, MAX(startedAtEpochMs) as lastPlayedAt
        FROM play_events
        GROUP BY songId
        ORDER BY lastPlayedAt DESC
        LIMIT :limit
        """
    )
    suspend fun recentlyPlayedSongIds(limit: Int = 20): List<RecentSongRow>

    /**
     * Songs ordered by play count, all-time. Feeds the "Your Mix" algorithm
     * alongside liked songs.
     */
    @Query(
        """
        SELECT songId, COUNT(*) as playCount
        FROM play_events
        GROUP BY songId
        ORDER BY playCount DESC
        LIMIT :limit
        """
    )
    suspend fun topPlayedSongIds(limit: Int = 50): List<TopSongRow>

    @Query("SELECT COUNT(*) as totalPlays, COALESCE(SUM(msPlayed),0) as totalMsPlayed FROM play_events WHERE artist = :artist")
    suspend fun statsForArtist(artist: String): GroupStatsRow?

    @Query("SELECT COUNT(*) as totalPlays, COALESCE(SUM(msPlayed),0) as totalMsPlayed FROM play_events WHERE albumId = :albumId")
    suspend fun statsForAlbum(albumId: Long): GroupStatsRow?

    @Query("SELECT COUNT(*) as totalPlays, COALESCE(SUM(msPlayed),0) as totalMsPlayed FROM play_events WHERE album = :albumTitle COLLATE NOCASE")
    suspend fun statsForAlbumTitle(albumTitle: String): GroupStatsRow?

    @Query(
        """
        SELECT songId, COUNT(*) as playCount
        FROM play_events
        WHERE artist = :artist
        GROUP BY songId
        ORDER BY playCount DESC
        LIMIT 1
        """
    )
    suspend fun topSongForArtist(artist: String): TopSongRow?

    @Query(
        """
        SELECT songId, COUNT(*) as playCount
        FROM play_events
        WHERE albumId = :albumId
        GROUP BY songId
        ORDER BY playCount DESC
        LIMIT 1
        """
    )
    suspend fun topSongForAlbum(albumId: Long): TopSongRow?

    @Query(
        """
        SELECT songId, COUNT(*) as playCount
        FROM play_events
        WHERE album = :albumTitle COLLATE NOCASE
        GROUP BY songId
        ORDER BY playCount DESC
        LIMIT 1
        """
    )
    suspend fun topSongForAlbumTitle(albumTitle: String): TopSongRow?

    /** Top played songs WITH title, for the Stats "Top categories: Song" tab. */
    @Query(
        """
        SELECT pe.songId as songId, s.title as title, COUNT(*) as playCount
        FROM play_events pe
        JOIN songs s ON s.mediaStoreId = pe.songId
        WHERE pe.startedAtEpochMs >= :sinceEpochMs
        GROUP BY pe.songId
        ORDER BY playCount DESC
        LIMIT :limit
        """
    )
    suspend fun topSongsWithTitles(sinceEpochMs: Long, limit: Int = 10): List<TopSongTitleRow>

    /** Top played albums by title, for the Stats "Top categories: Album" tab. */
    @Query(
        """
        SELECT album as label, COUNT(*) as playCount
        FROM play_events
        WHERE startedAtEpochMs >= :sinceEpochMs
        GROUP BY album COLLATE NOCASE
        ORDER BY playCount DESC
        LIMIT :limit
        """
    )
    suspend fun topAlbums(sinceEpochMs: Long, limit: Int = 10): List<LabeledPlayCount>

    /** Single most-repeated song within an exact time window — feeds the "yesterday" insight. */
    @Query(
        """
        SELECT pe.songId as songId, s.title as title, COUNT(*) as playCount
        FROM play_events pe
        JOIN songs s ON s.mediaStoreId = pe.songId
        WHERE pe.startedAtEpochMs >= :startEpochMs AND pe.startedAtEpochMs < :endEpochMs
        GROUP BY pe.songId
        ORDER BY playCount DESC
        LIMIT 1
        """
    )
    suspend fun topSongForDateRange(startEpochMs: Long, endEpochMs: Long): TopSongTitleRow?

    /** Monthly totals over the given window — feeds the yearly Area Chart (one area per month). */
    @Query(
        """
        SELECT strftime('%Y-%m', startedAtEpochMs / 1000, 'unixepoch', 'localtime') as monthLabel,
               COUNT(*) as playCount
        FROM play_events
        WHERE startedAtEpochMs >= :sinceEpochMs
        GROUP BY monthLabel
        ORDER BY monthLabel ASC
        """
    )
    suspend fun playsByMonth(sinceEpochMs: Long): List<MonthPlayCount>
}

data class RecentSongRow(val songId: Long, val lastPlayedAt: Long)
data class TopSongRow(val songId: Long, val playCount: Int)
data class GroupStatsRow(val totalPlays: Int, val totalMsPlayed: Long)
data class TopSongTitleRow(val songId: Long, val title: String, val playCount: Int)
data class LabeledPlayCount(val label: String, val playCount: Int)
data class MonthPlayCount(val monthLabel: String, val playCount: Int)

