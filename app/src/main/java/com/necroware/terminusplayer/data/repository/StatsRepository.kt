package com.necroware.terminusplayer.data.repository

import com.necroware.terminusplayer.data.database.dao.ArtistPlayCount
import com.necroware.terminusplayer.data.database.dao.DayPlayCount
import com.necroware.terminusplayer.data.database.dao.HourHistogramRow
import com.necroware.terminusplayer.data.database.dao.PlayEventDao
import com.necroware.terminusplayer.data.database.dao.SongDao
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
    private val playEventDao: PlayEventDao,
    private val songDao: SongDao
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

    /**
     * Dynamically generates a tailored custom terminal log message for the selected
     * StatsRange (TODAY, WEEK, MONTH, YEAR, ALL_TIME) and TopCategory (SONG, ARTIST, ALBUM).
     */
    suspend fun getCustomMessageForRangeAndCategory(range: StatsRange, category: TopCategory): String {
        val since = sinceEpochMsFor(range)
        return when (category) {
            TopCategory.SONG -> {
                val topSongRow = playEventDao.topSongsWithTitles(since, limit = 1).firstOrNull()
                if (topSongRow != null) {
                    val songEntity = songDao.getById(topSongRow.songId)
                    val title = songEntity?.title ?: topSongRow.title
                    val artist = songEntity?.artist ?: "Artist"
                    val album = songEntity?.album ?: "Album"
                    val playCount = topSongRow.playCount
                    val plural = if (playCount == 1) "play" else "plays"

                    when (range) {
                        StatsRange.TODAY -> "LOG_EVENT // Today's top track: '$title' by $artist on '$album' with $playCount $plural."
                        StatsRange.WEEK -> "FACILITY_NOTICE // Weekly top track: '$title' by $artist on '$album' leading matrix with $playCount $plural."
                        StatsRange.MONTH -> "MONTHLY_SUMMARY // Top track this month: '$title' by $artist on '$album' ($playCount total $plural)."
                        StatsRange.YEAR -> "ANNUAL_RECAP // Year's top track: '$title' by $artist on '$album' with $playCount $plural."
                        StatsRange.ALL_TIME -> "ALL_TIME_INDEX // All-time top track: '$title' by $artist on '$album' with $playCount total $plural!"
                    }
                } else {
                    getFallbackMessage(range, category)
                }
            }

            TopCategory.ARTIST -> {
                val topArtistRow = playEventDao.topArtists(since, limit = 1).firstOrNull()
                if (topArtistRow != null) {
                    val artist = topArtistRow.artist
                    val playCount = topArtistRow.playCount
                    val hours = (topArtistRow.msPlayed / 3600000L).coerceAtLeast(1)
                    val topSong = playEventDao.topSongForArtist(artist)?.let { songDao.getById(it.songId) }
                    val songTitleStr = topSong?.title?.let { " featuring '$it'" } ?: ""
                    val albumStr = topSong?.album?.let { " on '$it'" } ?: ""
                    val plural = if (playCount == 1) "play" else "plays"

                    when (range) {
                        StatsRange.TODAY -> "LOG_EVENT // Today's top artist: $artist$songTitleStr$albumStr leading with $playCount $plural."
                        StatsRange.WEEK -> "TELEMETRY // Weekly top artist: $artist$songTitleStr (${hours}h, $playCount $plural). Listener dedication index high."
                        StatsRange.MONTH -> "MONTHLY_SUMMARY // Top artist this month: $artist (${hours}h total streaming time across $playCount $plural)."
                        StatsRange.YEAR -> "ANNUAL_RECAP // Top artist this year: $artist with ${hours}h total streaming time and $playCount $plural."
                        StatsRange.ALL_TIME -> "ALL_TIME_INDEX // All-time top artist: $artist accumulated ${hours}h of total listening time across $playCount $plural!"
                    }
                } else {
                    getFallbackMessage(range, category)
                }
            }

            TopCategory.ALBUM -> {
                val topAlbumRow = playEventDao.topAlbums(since, limit = 1).firstOrNull()
                if (topAlbumRow != null) {
                    val albumTitle = topAlbumRow.label
                    val playCount = topAlbumRow.playCount
                    val topSong = playEventDao.topSongForAlbumTitle(albumTitle)?.let { songDao.getById(it.songId) }
                    val artistStr = topSong?.artist?.let { " by $it" } ?: ""
                    val songTitleStr = topSong?.title?.let { " (top track: '$it')" } ?: ""
                    val plural = if (playCount == 1) "play" else "plays"

                    when (range) {
                        StatsRange.TODAY -> "LOG_EVENT // Today's top album: '$albumTitle'$artistStr$songTitleStr with $playCount $plural."
                        StatsRange.WEEK -> "FACILITY_NOTICE // Weekly top album: '$albumTitle'$artistStr leading Matrix index with $playCount $plural."
                        StatsRange.MONTH -> "MONTHLY_SUMMARY // Top album this month: '$albumTitle'$artistStr$songTitleStr ($playCount $plural)."
                        StatsRange.YEAR -> "ANNUAL_RECAP // Year's top album: '$albumTitle'$artistStr with $playCount playback cycles."
                        StatsRange.ALL_TIME -> "ALL_TIME_INDEX // All-time top album: '$albumTitle'$artistStr with $playCount total $plural!"
                    }
                } else {
                    getFallbackMessage(range, category)
                }
            }
        }
    }

    private suspend fun getFallbackMessage(range: StatsRange, category: TopCategory): String {
        val sampleSong = songDao.mostRecentlyAdded(1).firstOrNull()
        return if (sampleSong != null) {
            "SYSTEM_STATUS // [$range - ${category.name}]: No logged plays in this range yet. Library track ready: '${sampleSong.title}' by ${sampleSong.artist}."
        } else {
            "SYSTEM_STATUS // [$range - ${category.name}]: Terminal engine ready. Play tracks to log metric data."
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
     * Highly contextual, witty Terminus metric log messages dynamically constructed
     * with Song Title, Artist Name, and Album Title metadata.
     */
    suspend fun generateInsights(): List<String> {
        val insights = mutableListOf<String>()
        val now = System.currentTimeMillis()
        val dayMs = TimeUnit.DAYS.toMillis(1)
        val hourMs = TimeUnit.HOURS.toMillis(1)

        val sinceWeek = now - dayMs * 7
        val weekArtists = playEventDao.topArtists(sinceWeek, limit = 5)
        val todayDayEpoch = (now / dayMs) * dayMs
        val topSongTodayRow = playEventDao.topSongsWithTitles(todayDayEpoch, limit = 1).firstOrNull()

        // 1. The Single Song Loop
        if (topSongTodayRow != null && topSongTodayRow.playCount >= 5) {
            val songEntity = songDao.getById(topSongTodayRow.songId)
            val title = songEntity?.title ?: topSongTodayRow.title
            val artist = songEntity?.artist ?: "Artist"
            val album = songEntity?.album ?: "Album"
            val count = topSongTodayRow.playCount
            val templates = listOf(
                "SYS_WARN // Track '$title' from album '$album' by $artist looped $count times. Dopamine levels nominal.",
                "LOG_EVENT // Buffer lock on '$title' ($album - $artist). $count playback cycles recorded today.",
                "DIAGNOSTIC // Track '$title' by $artist wearing physical groove into flash storage ($album).",
                "KERNEL_NOTICE // Stream '$title' ($artist) active for ${count * 3}m continuously.",
                "CRITICAL_REPETITION // '$title' by $artist ($album) played $count times. System intervention suggested."
            )
            insights += templates.random()
        }

        // 2. Artist Loyalty / Binge
        weekArtists.firstOrNull()?.let { top ->
            val hours = (top.msPlayed / hourMs).coerceAtLeast(1)
            val artist = top.artist
            val topSongRow = playEventDao.topSongForArtist(artist)
            val topSongEntity = topSongRow?.let { songDao.getById(it.songId) }
            val album = topSongEntity?.album ?: "Library Collection"
            val songTitle = topSongEntity?.title
            val ramPct = (30..65).random()

            val albumText = if (songTitle != null) " ('$songTitle' on '$album')" else " on '$album'"
            val templates = listOf(
                "FACILITY_NOTICE // $artist$albumText occupying $ramPct% of active memory. $hours hours logged.",
                "TELEMETRY // $hours hours of $artist$albumText. High listener dedication index detected.",
                "SYSTEM_LOG // $artist ($album) stream duration exceeded $hours hours this week.",
                "OVERLOAD // Active playback stream $artist$albumText leading matrix index."
            )
            insights += templates.random()
        }

        // 3. Night Shift / Degenerate Hours
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        if (hour in 2..5) {
            val lastTracks = playEventDao.topSongsWithTitles(now - hourMs, limit = 1)
            val topTrackRow = lastTracks.firstOrNull()
            val songEntity = topTrackRow?.let { songDao.getById(it.songId) } ?: songDao.mostRecentlyAdded(1).firstOrNull()
            if (songEntity != null) {
                val currentTrack = songEntity.title
                val artistName = songEntity.artist
                val albumName = songEntity.album
                val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(cal.time)
                val templates = listOf(
                    "TIME_STAMP [$timeStr] // Currently playing '$currentTrack' from '$albumName' by $artistName.",
                    "NIGHT_SHIFT_LOG // $artistName - '$currentTrack' ($albumName) streaming at $timeStr.",
                    "KERNEL_DIAGNOSTIC // Sleep state absent. Defaulting to '$currentTrack' by $artistName ($albumName).",
                    "SYS_ALERT // Early morning stream: '$currentTrack' ($albumName) by $artistName."
                )
                insights += templates.random()
            }
        }

        // 4. Major Lifetime Milestones
        val topAllTime = playEventDao.topArtists(0L, limit = 5)
        val totalMsAllTime = playEventDao.totalMsPlayed(0L).coerceAtLeast(1L)
        topAllTime.forEach { artist ->
            val totalHours = artist.msPlayed / hourMs
            val pct = ((artist.msPlayed.toDouble() / totalMsAllTime) * 100).toInt().coerceIn(1, 99)
            val topSongRow = playEventDao.topSongForArtist(artist.artist)
            val topSongEntity = topSongRow?.let { songDao.getById(it.songId) }
            val albumStr = topSongEntity?.album?.let { " ('$it')" } ?: ""
            when {
                totalHours >= 500 -> insights += "FILE_CORRUPTION: Local storage designated for '${artist.artist}'$albumStr."
                totalHours >= 100 -> insights += "CENTURY_MARK: 100 hours of ${artist.artist}$albumStr logged."
                totalHours >= 50 -> insights += "ACHIEVEMENT_UNLOCKED: 50 Hours of ${artist.artist}$albumStr."
                totalHours >= 10 -> insights += "STATUS_UPDATE: $pct% of playback history spent on ${artist.artist}$albumStr."
            }
        }

        if (insights.isEmpty()) {
            val topSongRow = playEventDao.topSongsWithTitles(0L, limit = 1).firstOrNull()
            val topSongEntity = topSongRow?.let { songDao.getById(it.songId) } ?: songDao.mostRecentlyAdded(1).firstOrNull()
            if (topSongEntity != null) {
                insights += "METRIC // TOP_TRACK: '${topSongEntity.title}' by ${topSongEntity.artist} from '${topSongEntity.album}' leading playback log index."
            } else {
                insights += "METRIC // SYSTEM: Library indexing active. Select a track to initialize metric tracking matrix."
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
