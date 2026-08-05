package com.necroware.terminusplayer.data.prefs

enum class ThemePresetId {
    TERMINAL,
    VECTOR,
    REBECCA,
    DUNE,
    HEX,
    LUCY,
    MAINE,
    FLATLINE,
    WIZ
}

enum class SortField { TITLE, ARTIST, ALBUM, DATE_ADDED, DURATION }
enum class SortDirection { ASC, DESC }

enum class PlaybackArtStyle {
    STANDARD,
    CASSETTE,
    REEL_TO_REEL,
    VINYL,
    VHS
}

data class LibrarySortOrder(
    val field: SortField = SortField.TITLE,
    val direction: SortDirection = SortDirection.ASC
)

/** 5-band graphic EQ, gains in dB clamped to [-12, 12]. Bands correspond to
 *  roughly 60Hz / 230Hz / 910Hz / 3.6kHz / 14kHz, matching a typical Android
 *  android.media.audiofx.Equalizer's 5-band layout. */
data class EqualizerSettings(
    val enabled: Boolean = false,
    val bandGainsDb: List<Int> = List(5) { 0 }
)

data class CrossfadeSettings(
    val enabled: Boolean = false,
    val durationMs: Int = 4000
)

data class UserPreferences(
    val themeId: ThemePresetId = ThemePresetId.TERMINAL,
    val librarySortOrder: LibrarySortOrder = LibrarySortOrder(),
    val equalizer: EqualizerSettings = EqualizerSettings(),
    val crossfade: CrossfadeSettings = CrossfadeSettings(),
    val preferHardwareDecoder: Boolean = true,
    val playbackArtStyle: PlaybackArtStyle = PlaybackArtStyle.STANDARD,
    val lastPlayedSongId: Long? = null,
    val lastPlayedPositionMs: Long = 0L
)
