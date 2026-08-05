package com.necroware.terminusplayer.data.repository

import com.necroware.terminusplayer.data.database.dao.ArtistPlayCount
import com.necroware.terminusplayer.data.database.dao.DayPlayCount
import com.necroware.terminusplayer.data.database.dao.HourHistogramRow
import com.necroware.terminusplayer.data.database.dao.PlayEventDao
import com.necroware.terminusplayer.data.database.entity.PlayEventEntity
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

enum class StatsRange(val label: String) {
    TODAY("TODAY"),
    WEEK("WEEK"),
    MONTH("MONTH"),
    YEAR("YEAR"),
    ALL_TIME("ALL_TIME")
}

enum class TopCategory(val label: String) {
    SONG("SONG"),
    ALBUM("ALBUM"),
    ARTIST("ARTIST")
}

data class TopCategoryItem(val label: String, val playCount: Int)

data class StatsSummary(
    val totalPlays: Int,
    val totalMsPlayed: Long,
    val topArtists: List<ArtistPlayCount>,
    val playsByDay: List<DayPlayCount>,
    val playsByHour: List<HourHistogramRow>
)

/** Mini stats shown on Album/Artist detail screens. */
data class GroupListenStats(
    val totalPlays: Int,
    val totalMsPlayed: Long,
    val topSongId: Long?
)

data class SessionStats(
    val totalSessions: Int,
    val avgSessionMs: Long,
    val longestSessionMs: Long
)

@Singleton
class StatsRepository @Inject constructor(
    private val playEventDao: PlayEventDao
) {

    suspend fun recordPlay(
        songId: Long,
        artist: String,
        album: String,
        albumId: Long,
        msPlayed: Long,
        completed: Boolean
    ) {
        playEventDao.insert(
            PlayEventEntity(
                songId = songId,
                artist = artist,
                album = album,
                albumId = albumId,
                startedAtEpochMs = System.currentTimeMillis(),
                msPlayed = msPlayed,
                completed = completed
            )
        )
    }

    suspend fun getSummary(range: StatsRange): StatsSummary {
        val since = sinceEpochMsFor(range)
        return StatsSummary(
            totalPlays = playEventDao.totalPlays(since),
            totalMsPlayed = playEventDao.totalMsPlayed(since),
            topArtists = playEventDao.topArtists(since),
            playsByDay = playEventDao.playsByDay(since),
            playsByHour = playEventDao.playsByHourOfDay(since)
        )
    }

    suspend fun getTopCategory(range: StatsRange, category: TopCategory, limit: Int = 10): List<TopCategoryItem> {
        val since = sinceEpochMsFor(range)
        return when (category) {
            TopCategory.SONG -> playEventDao.topSongsWithTitles(since, limit).map { TopCategoryItem(it.title, it.playCount) }
            TopCategory.ALBUM -> playEventDao.topAlbums(since, limit).map { TopCategoryItem(it.label, it.playCount) }
            TopCategory.ARTIST -> playEventDao.topArtists(since, limit).map { TopCategoryItem(it.artist, it.playCount) }
        }
    }

    /** Top artists by play count within the current MONTH — feeds the Month tab's Pie Chart. */
    suspend fun getMonthlyArtistDistribution(limit: Int = 6): List<Pair<String, Int>> {
        val since = sinceEpochMsFor(StatsRange.MONTH)
        return playEventDao.topArtists(since, limit).map { it.artist to it.playCount }
    }

    /** Monthly totals over the past year — feeds the Year tab's Area Chart (one area per month). */
    suspend fun getYearlyMonthlyTotals(): List<Pair<String, Int>> {
        val since = sinceEpochMsFor(StatsRange.YEAR)
        return playEventDao.playsByMonth(since).map { it.monthLabel to it.playCount }
    }

    suspend fun getStatsForArtist(artist: String): GroupListenStats {
        val summary = playEventDao.statsForArtist(artist)
        val topSong = playEventDao.topSongForArtist(artist)
        return GroupListenStats(
            totalPlays = summary?.totalPlays ?: 0,
            totalMsPlayed = summary?.totalMsPlayed ?: 0L,
            topSongId = topSong?.songId
        )
    }

    suspend fun getStatsForAlbum(albumTitle: String): GroupListenStats {
        val summary = playEventDao.statsForAlbumTitle(albumTitle)
        val topSong = playEventDao.topSongForAlbumTitle(albumTitle)
        return GroupListenStats(
            totalPlays = summary?.totalPlays ?: 0,
            totalMsPlayed = summary?.totalMsPlayed ?: 0L,
            topSongId = topSong?.songId
        )
    }

    /**
     * Groups raw plays into "sessions" using a simple gap heuristic: a new
     * session starts whenever more than 30 minutes pass between the end of
     * one play and the start of the next. Not a precise definition of a
     * listening session, but a reasonable, explainable approximation from
     * the data we actually have.
     */
    suspend fun getSessionStats(range: StatsRange): SessionStats {
        val since = sinceEpochMsFor(range)
        val events = playEventDao.observeEventsSince(since).first().sortedBy { it.startedAtEpochMs }
        if (events.isEmpty()) return SessionStats(0, 0L, 0L)

        val gapThresholdMs = TimeUnit.MINUTES.toMillis(30)
        val sessionDurations = mutableListOf<Long>()

        var sessionStart = events.first().startedAtEpochMs
        var sessionEnd = events.first().startedAtEpochMs + events.first().msPlayed

        for (i in 1 until events.size) {
            val event = events[i]
            val gap = event.startedAtEpochMs - sessionEnd
            if (gap > gapThresholdMs) {
                sessionDurations += (sessionEnd - sessionStart).coerceAtLeast(0L)
                sessionStart = event.startedAtEpochMs
            }
            sessionEnd = event.startedAtEpochMs + event.msPlayed
        }
        sessionDurations += (sessionEnd - sessionStart).coerceAtLeast(0L)

        return SessionStats(
            totalSessions = sessionDurations.size,
            avgSessionMs = sessionDurations.sum() / sessionDurations.size,
            longestSessionMs = sessionDurations.max()
        )
    }

    /**
     * A few lightweight, rule-based observations about recent listening —
     * intentionally simple heuristics over the same play_events data, not
     * a recommendation engine. Returns an empty list rather than forcing
     * something when there isn't enough signal yet.
     */
    suspend fun generateInsights(): List<String> {
        val insights = mutableListOf<String>()
        val now = System.currentTimeMillis()
        val dayMs = TimeUnit.DAYS.toMillis(1)

        val weekArtists = playEventDao.topArtists(now - dayMs * 7, limit = 5)
        val monthArtists = playEventDao.topArtists(now - dayMs * 30, limit = 10)

        weekArtists.firstOrNull()?.let { top ->
            if (top.playCount >= 5) {
                insights += "You're a big fan of ${top.artist} this week — ${top.playCount} plays."
            }
        }

        // "Yesterday" bucketed the same way playsByDay buckets days (UTC epoch-day),
        // for consistency with the rest of Stats rather than local-calendar midnight.
        val todayDayEpoch = (now / dayMs) * dayMs
        val yesterdayStart = todayDayEpoch - dayMs
        val yesterdayTopSong = playEventDao.topSongForDateRange(yesterdayStart, todayDayEpoch)
        if (yesterdayTopSong != null && yesterdayTopSong.playCount >= 3) {
            insights += "Yesterday you listened to ${yesterdayTopSong.title} ${yesterdayTopSong.playCount}x. That's dedication."
        }

        monthArtists
            .filter { m -> m.playCount >= 15 && weekArtists.none { w -> w.artist.equals(m.artist, ignoreCase = true) } }
            .firstOrNull()
            ?.let { stale ->
                insights += "You've listened to ${stale.artist} ${stale.playCount}x this month but haven't touched them in the past week."
            }

        return insights
    }

    private fun sinceEpochMsFor(range: StatsRange): Long {
        val now = System.currentTimeMillis()
        return when (range) {
            // Local midnight, NOT "last 24 hours" — a rolling window never
            // resets, which reads as wrong for something literally called
            // "Today". Week/Month/Year stay rolling windows for now.
            StatsRange.TODAY -> {
                val cal = java.util.Calendar.getInstance()
                cal.set(java.util.Calendar.HOUR_OF_DAY, 0)
                cal.set(java.util.Calendar.MINUTE, 0)
                cal.set(java.util.Calendar.SECOND, 0)
                cal.set(java.util.Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            StatsRange.WEEK -> now - TimeUnit.DAYS.toMillis(7)
            StatsRange.MONTH -> now - TimeUnit.DAYS.toMillis(30)
            StatsRange.YEAR -> now - TimeUnit.DAYS.toMillis(365)
            StatsRange.ALL_TIME -> 0L
        }
    }
}
