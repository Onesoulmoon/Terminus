package com.necroware.terminusplayer.data.repository

import com.necroware.terminusplayer.data.database.dao.ArtistPlayCount
import com.necroware.terminusplayer.data.database.dao.DayPlayCount
import com.necroware.terminusplayer.data.database.dao.HourHistogramRow
import com.necroware.terminusplayer.data.database.dao.PlayEventDao
import com.necroware.terminusplayer.data.database.entity.PlayEventEntity
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
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
     * Highly contextual, witty Terminus metric log messages based on listening habits.
     */
    suspend fun generateInsights(): List<String> {
        val insights = mutableListOf<String>()
        val now = System.currentTimeMillis()
        val dayMs = TimeUnit.DAYS.toMillis(1)
        val hourMs = TimeUnit.HOURS.toMillis(1)

        val sinceWeek = now - dayMs * 7
        val weekArtists = playEventDao.topArtists(sinceWeek, limit = 5)
        val todayDayEpoch = (now / dayMs) * dayMs
        val topSongToday = playEventDao.topSongsWithTitles(todayDayEpoch, limit = 1).firstOrNull()

        // 1. The Single Song Loop (Played 1 song 5+ times / looped)
        if (topSongToday != null && topSongToday.playCount >= 5) {
            val title = topSongToday.title
            val count = topSongToday.playCount
            val snark = listOf(
                "SYS_WARN // Track '$title' has been looped $count times. Geez, Is everything okay at home dude?",
                "LOG_EVENT: Buffer lock detected on '$title'. Your dopamine receptors are officially cooked.",
                "DIAGNOSTIC: '$title' is wearing a literal physical groove into your flash storage. Give it a rest man, common!",
                "KERNEL_NOTICE: Audio stream '$title' active for ${count * 3}m. We get it. It’s a vibe. Now chill fahhhh...",
                "CRITICAL_REPETITION: Playing '$title' again will trigger automatic system intervention, because what do you mean?"
            )
            insights += snark.random()
        }

        // 2. The Artist Loyalty / Binge (Played 1 artist for 3+ hours or top artist this week)
        weekArtists.firstOrNull()?.let { top ->
            val hours = (top.msPlayed / hourMs).coerceAtLeast(1)
            val artist = top.artist
            val ramPct = (30..65).random()
            val snark = listOf(
                "FACILITY_NOTICE: $artist now occupies $ramPct% of your remaining brain RAM. Not that you had much to begin with lol.",
                "TELEMETRY: $hours continuous hours of $artist. We are legally required to ask if you handle the divorce well.",
                "SYSTEM_LOG: $artist has played sooooooo long the kernel is considering charging them rent at this point.",
                "OVERLOAD: Damn dude, You’ve listened to $artist more than their own mother has this week.",
                "ALERT: $artist stream duration exceeded. The DAC is sweating."
            )
            insights += snark.random()
        }

        // 3. Night Shift / Degenerate Hours (Listening between 2 AM – 5 AM)
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        if (hour in 2..5) {
            val lastTracks = playEventDao.topSongsWithTitles(now - hourMs, limit = 1)
            val currentTrack = lastTracks.firstOrNull()?.title ?: "Turban"
            val artistName = weekArtists.firstOrNull()?.artist ?: "Yeat"
            val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(cal.time)
            val snark = listOf(
                "TIME_STAMP [$timeStr]: Playing '$currentTrack' right now strongly points to a circadian system failure. And unemployment...",
                "NIGHT_SHIFT_LOG: No one listens to $artistName at $timeStr for healthy reasons.",
                "KERNEL_DIAGNOSTIC: Sleep.exe not found. Defaulting to '$currentTrack' loop.",
                "SYS_ALERT: Sun rises in 2 hours. Your choice of $artistName is not helping at all."
            )
            insights += snark.random()
        }

        // 4. Major Lifetime Milestones (50h / 100h / 500h total on an Artist)
        val topAllTime = playEventDao.topArtists(0L, limit = 5)
        val totalMsAllTime = playEventDao.totalMsPlayed(0L).coerceAtLeast(1L)
        topAllTime.forEach { artist ->
            val totalHours = artist.msPlayed / hourMs
            val pct = ((artist.msPlayed.toDouble() / totalMsAllTime) * 100).toInt().coerceIn(1, 99)
            when {
                totalHours >= 500 -> insights += "FILE_CORRUPTION: Local storage renamed to '${artist.artist}_DEDICATED_SERVER'."
                totalHours >= 100 -> insights += "CENTURY_MARK: 100 hours of ${artist.artist} logged. System kernel has officially converted to their cult."
                totalHours >= 50 -> insights += "ACHIEVEMENT_UNLOCKED: 50 Hours of ${artist.artist}. You are officially on their emergency contact list."
                totalHours >= 10 -> insights += "STATUS_UPDATE: You have spent $pct% of your entire year listening to ${artist.artist}. No regrets detected."
            }
        }

        if (insights.isEmpty()) {
            weekArtists.firstOrNull()?.let { top ->
                insights += "METRIC // TOP_ARTIST: '${top.artist}' leading the log tracking matrix index with ${top.playCount} recorded playback cycles this week."
            } ?: run {
                insights += "METRIC // TOP_ARTIST: 'Yeat' leading the log tracking matrix index with 9 recorded playback cycles this week."
            }
        }

        return insights.distinct()
    }

    private fun sinceEpochMsFor(range: StatsRange): Long {
        val now = System.currentTimeMillis()
        return when (range) {
            StatsRange.TODAY -> {
                val cal = Calendar.getInstance()
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis
            }
            StatsRange.WEEK -> now - TimeUnit.DAYS.toMillis(7)
            StatsRange.MONTH -> now - TimeUnit.DAYS.toMillis(30)
            StatsRange.YEAR -> now - TimeUnit.DAYS.toMillis(365)
            StatsRange.ALL_TIME -> 0L
        }
    }
}
