package com.necroware.terminusplayer.ui.navigation

import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class Destination(val route: String, val label: String) {
    data object Home : Destination("home", "HOME")
    data object Library : Destination("library", "LIBRARY")
    data object Playlists : Destination("playlists", "PLAYLISTS")
    data object Stats : Destination("stats", "STATS")
    data object Settings : Destination("settings", "SETTINGS")
    data object NowPlaying : Destination("now_playing", "NOW PLAYING")
    data object Search : Destination("search", "SEARCH")

    /** Route pattern registered with NavHost. Album is keyed by TITLE, not
     *  MediaStore albumId — MediaStore fragments one album into several
     *  albumIds when per-track artist tags differ (e.g. feature credits),
     *  so grouping/navigating by title is what actually merges them back
     *  into a single album view. */
    data object AlbumDetail : Destination("album_detail/{albumTitle}", "ALBUM") {
        fun createRoute(albumTitle: String) = "album_detail/${encodeRouteSegment(albumTitle)}"
    }

    /** Route pattern registered with NavHost. Use [createRoute] to navigate to a specific artist. */
    data object ArtistDetail : Destination("artist_detail/{artist}", "ARTIST") {
        fun createRoute(artist: String) = "artist_detail/${encodeRouteSegment(artist)}"
    }

    /** Route pattern registered with NavHost. kind is a PlaylistKind enum name (LIKED/RECENT/MOST_PLAYED). */
    data object PlaylistDetail : Destination("playlist_detail/{kind}", "PLAYLIST") {
        fun createRoute(kind: String) = "playlist_detail/$kind"
    }
}

/**
 * URLEncoder.encode() converts spaces to '+' (application/x-www-form-urlencoded
 * convention), but Navigation-Compose decodes path arguments with standard
 * percent-decoding, which does NOT turn '+' back into a space. Any title
 * containing a space (i.e. almost every real album/artist name) silently
 * broke as a result — the SQL lookup downstream never matched. Re-encoding
 * spaces as literal %20 instead fixes the round trip.
 */
private fun encodeRouteSegment(value: String): String =
    URLEncoder.encode(value, StandardCharsets.UTF_8.name()).replace("+", "%20")

val bottomNavItems = listOf(
    Destination.Home,
    Destination.Library,
    Destination.Playlists,
    Destination.Stats,
    Destination.Settings
)
