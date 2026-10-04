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
    DYNAMIC // Added for Album Art Theme
}

enum class SortField { TITLE, ARTIST, ALBUM, DATE_ADDED, DURATION }
enum class SortDirection { ASC, DESC }

enum class VisualizerMode {
    WAVE_MESH,
    DOT_MATRIX,
    BARS,
    SYMMETRIC_BARS
}

enum class DynamicThemeMode {
    DARK,
    LIGHT
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
    val bandGainsDb: List<Int> = List(10) { 0 },
    val preampGainDb: Float = 0f,
    val highPassEnabled: Boolean = false,
    val highPassFreq: Float = 40f,
    val lowPassEnabled: Boolean = false,
    val lowPassFreq: Float = 20000f
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
    val lastPlayedSongId: Long? = null,
    val lastPlayedPositionMs: Long = 0L,
    val lastPlayedQueueIds: List<Long> = emptyList(),
    val useDynamicTheme: Boolean = false,
    val dynamicThemeMode: DynamicThemeMode = DynamicThemeMode.DARK,
    val useCustomArtAlternatives: Boolean = false,
    val albumArtMode: AlbumArtMode = AlbumArtMode.PIXELATED,
    val shuffleEnabled: Boolean = false,
    val repeatMode: Int = 0,
    val audioMonitorEnabled: Boolean = true,
    val visualizerMode: VisualizerMode = VisualizerMode.BARS,
    val matrixBgEnabled: Boolean = false,
    val usbExclusiveHqEnabled: Boolean = false,
    val realtimeVisualizerEnabled: Boolean = false
)
