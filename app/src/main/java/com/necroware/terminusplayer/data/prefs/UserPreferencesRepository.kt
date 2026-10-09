package com.necroware.terminusplayer.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private object Keys {
    val THEME_ID = stringPreferencesKey("theme_id")
    val SORT_FIELD = stringPreferencesKey("sort_field")
    val SORT_DIRECTION = stringPreferencesKey("sort_direction")
    val EQ_ENABLED = booleanPreferencesKey("eq_enabled")
    val EQ_BAND_PREFIX = "eq_band_"
    val CROSSFADE_ENABLED = booleanPreferencesKey("crossfade_enabled")
    val CROSSFADE_DURATION_MS = intPreferencesKey("crossfade_duration_ms")
    val PREFER_HW_DECODER = booleanPreferencesKey("prefer_hw_decoder")
    val LAST_PLAYED_SONG_ID = longPreferencesKey("last_played_song_id")
    val LAST_PLAYED_POSITION_MS = longPreferencesKey("last_played_position_ms")
    val LAST_PLAYED_QUEUE_IDS = stringPreferencesKey("last_played_queue_ids") // Stored as comma-separated string
    val USE_DYNAMIC_THEME = booleanPreferencesKey("use_dynamic_theme")
    val DYNAMIC_THEME_MODE = stringPreferencesKey("dynamic_theme_mode")
    val USE_CUSTOM_ART_ALTERNATIVES = booleanPreferencesKey("use_custom_art_alternatives")
    val ALBUM_ART_MODE = stringPreferencesKey("album_art_mode")
    val SHUFFLE_ENABLED = booleanPreferencesKey("shuffle_enabled")
    val REPEAT_MODE = intPreferencesKey("repeat_mode")
    val AUDIO_MONITOR_ENABLED = booleanPreferencesKey("audio_monitor_enabled")
    val VISUALIZER_MODE = stringPreferencesKey("visualizer_mode")
    val MATRIX_BG_ENABLED = booleanPreferencesKey("matrix_bg_enabled")
    val USB_EXCLUSIVE_HQ_ENABLED = booleanPreferencesKey("usb_exclusive_hq_enabled")
    val REALTIME_VISUALIZER_ENABLED = booleanPreferencesKey("realtime_visualizer_enabled")
    val EQ_PREAMP_GAIN = androidx.datastore.preferences.core.floatPreferencesKey("eq_preamp_gain")
    val EQ_HP_ENABLED = booleanPreferencesKey("eq_hp_enabled")
    val EQ_HP_FREQ = androidx.datastore.preferences.core.floatPreferencesKey("eq_hp_freq")
    val EQ_LP_ENABLED = booleanPreferencesKey("eq_lp_enabled")
    val EQ_LP_FREQ = androidx.datastore.preferences.core.floatPreferencesKey("eq_lp_freq")
    val TIMELINE_STYLE = stringPreferencesKey("timeline_style")
    val EXCLUDED_FOLDERS = stringSetPreferencesKey("excluded_folders")
}

private fun eqBandKey(index: Int) = intPreferencesKey("${Keys.EQ_BAND_PREFIX}$index")

@Singleton
class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    val preferences: Flow<UserPreferences> = dataStore.data.map { prefs -> prefs.toUserPreferences() }

    suspend fun setTheme(themeId: ThemePresetId) {
        dataStore.edit { it[Keys.THEME_ID] = themeId.name }
    }

    suspend fun setSortOrder(order: LibrarySortOrder) {
        dataStore.edit {
            it[Keys.SORT_FIELD] = order.field.name
            it[Keys.SORT_DIRECTION] = order.direction.name
        }
    }

    suspend fun setEqualizerEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.EQ_ENABLED] = enabled }
    }

    suspend fun setEqualizerBand(index: Int, gainDb: Int) {
        dataStore.edit { it[eqBandKey(index)] = gainDb }
    }

    suspend fun setEqualizerBands(gainsDb: List<Int>) {
        dataStore.edit { prefs -> gainsDb.forEachIndexed { index, gain -> prefs[eqBandKey(index)] = gain } }
    }

    suspend fun setCrossfadeEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.CROSSFADE_ENABLED] = enabled }
    }

    suspend fun setCrossfadeDurationMs(durationMs: Int) {
        dataStore.edit { it[Keys.CROSSFADE_DURATION_MS] = durationMs }
    }

    suspend fun setPreferHardwareDecoder(enabled: Boolean) {
        dataStore.edit { it[Keys.PREFER_HW_DECODER] = enabled }
    }

    suspend fun setLastPlayed(songId: Long?, positionMs: Long, queueIds: List<Long> = emptyList()) {
        dataStore.edit { prefs ->
            if (songId != null) {
                prefs[Keys.LAST_PLAYED_SONG_ID] = songId
            } else {
                prefs.remove(Keys.LAST_PLAYED_SONG_ID)
            }
            prefs[Keys.LAST_PLAYED_POSITION_MS] = positionMs
            if (queueIds.isNotEmpty()) {
                prefs[Keys.LAST_PLAYED_QUEUE_IDS] = queueIds.joinToString(",")
            } else {
                prefs.remove(Keys.LAST_PLAYED_QUEUE_IDS)
            }
        }
    }

    suspend fun setUseDynamicTheme(enabled: Boolean) {
        dataStore.edit { it[Keys.USE_DYNAMIC_THEME] = enabled }
    }

    suspend fun setDynamicThemeMode(mode: DynamicThemeMode) {
        dataStore.edit { it[Keys.DYNAMIC_THEME_MODE] = mode.name }
    }

    suspend fun setUseCustomArtAlternatives(enabled: Boolean) {
        dataStore.edit { it[Keys.USE_CUSTOM_ART_ALTERNATIVES] = enabled }
    }

    suspend fun setAlbumArtMode(mode: AlbumArtMode) {
        dataStore.edit { it[Keys.ALBUM_ART_MODE] = mode.name }
    }

    suspend fun setShuffleEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.SHUFFLE_ENABLED] = enabled }
    }

    suspend fun setRepeatMode(mode: Int) {
        dataStore.edit { it[Keys.REPEAT_MODE] = mode }
    }

    suspend fun setAudioMonitorEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.AUDIO_MONITOR_ENABLED] = enabled }
    }

    suspend fun setVisualizerMode(mode: VisualizerMode) {
        dataStore.edit { it[Keys.VISUALIZER_MODE] = mode.name }
    }

    suspend fun setMatrixBgEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.MATRIX_BG_ENABLED] = enabled }
    }

    suspend fun setUsbExclusiveHqEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.USB_EXCLUSIVE_HQ_ENABLED] = enabled }
    }

    suspend fun setRealtimeVisualizerEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.REALTIME_VISUALIZER_ENABLED] = enabled }
    }

    suspend fun setPreampGain(gain: Float) {
        dataStore.edit { it[Keys.EQ_PREAMP_GAIN] = gain }
    }

    suspend fun setHighPassEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.EQ_HP_ENABLED] = enabled }
    }

    suspend fun setHighPassFreq(freq: Float) {
        dataStore.edit { it[Keys.EQ_HP_FREQ] = freq }
    }

    suspend fun setLowPassEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.EQ_LP_ENABLED] = enabled }
    }

    suspend fun setLowPassFreq(freq: Float) {
        dataStore.edit { it[Keys.EQ_LP_FREQ] = freq }
    }

    suspend fun setTimelineStyle(style: TimelineStyle) {
        dataStore.edit { it[Keys.TIMELINE_STYLE] = style.name }
    }

    suspend fun setExcludedFolders(folders: Set<String>) {
        dataStore.edit { it[Keys.EXCLUDED_FOLDERS] = folders }
    }

    suspend fun addExcludedFolder(folder: String) {
        dataStore.edit { prefs ->
            val current = prefs[Keys.EXCLUDED_FOLDERS] ?: setOf("WhatsApp", "WhatsApp Audio", "WhatsApp Voice Notes", "Recordings", "CallRecord")
            prefs[Keys.EXCLUDED_FOLDERS] = current + folder
        }
    }

    suspend fun removeExcludedFolder(folder: String) {
        dataStore.edit { prefs ->
            val current = prefs[Keys.EXCLUDED_FOLDERS] ?: emptySet()
            prefs[Keys.EXCLUDED_FOLDERS] = current - folder
        }
    }

    suspend fun clearExcludedFolders() {
        dataStore.edit { prefs ->
            prefs[Keys.EXCLUDED_FOLDERS] = emptySet()
        }
    }

    private fun Preferences.toUserPreferences(): UserPreferences {
        val defaults = UserPreferences()
        val themeId = this[Keys.THEME_ID]?.let { runCatching { ThemePresetId.valueOf(it) }.getOrNull() }
            ?: defaults.themeId
        val sortField = this[Keys.SORT_FIELD]?.let { runCatching { SortField.valueOf(it) }.getOrNull() }
            ?: defaults.librarySortOrder.field
        val sortDirection = this[Keys.SORT_DIRECTION]?.let { runCatching { SortDirection.valueOf(it) }.getOrNull() }
            ?: defaults.librarySortOrder.direction
        val albumArtMode = this[Keys.ALBUM_ART_MODE]?.let { runCatching { AlbumArtMode.valueOf(it) }.getOrNull() }
            ?: defaults.albumArtMode
        val dynamicThemeMode = this[Keys.DYNAMIC_THEME_MODE]?.let { runCatching { DynamicThemeMode.valueOf(it) }.getOrNull() }
            ?: defaults.dynamicThemeMode
        val visualizerMode = this[Keys.VISUALIZER_MODE]?.let { runCatching { VisualizerMode.valueOf(it) }.getOrNull() }
            ?: defaults.visualizerMode
        val matrixBgEnabled = this[Keys.MATRIX_BG_ENABLED] ?: defaults.matrixBgEnabled
        val eqBands = List(10) { index -> this[eqBandKey(index)] ?: 0 } // Expanded to 10 for better EQ

        val queueIds = this[Keys.LAST_PLAYED_QUEUE_IDS]?.split(",")?.mapNotNull { it.toLongOrNull() } ?: emptyList()

        val timelineStyle = this[Keys.TIMELINE_STYLE]?.let { runCatching { TimelineStyle.valueOf(it) }.getOrNull() }
            ?: defaults.timelineStyle
        val excludedFolders = this[Keys.EXCLUDED_FOLDERS] ?: defaults.excludedFolders

        return UserPreferences(
            themeId = themeId,
            librarySortOrder = LibrarySortOrder(sortField, sortDirection),
            equalizer = EqualizerSettings(
                enabled = this[Keys.EQ_ENABLED] ?: defaults.equalizer.enabled,
                bandGainsDb = eqBands,
                preampGainDb = this[Keys.EQ_PREAMP_GAIN] ?: defaults.equalizer.preampGainDb,
                highPassEnabled = this[Keys.EQ_HP_ENABLED] ?: defaults.equalizer.highPassEnabled,
                highPassFreq = this[Keys.EQ_HP_FREQ] ?: defaults.equalizer.highPassFreq,
                lowPassEnabled = this[Keys.EQ_LP_ENABLED] ?: defaults.equalizer.lowPassEnabled,
                lowPassFreq = this[Keys.EQ_LP_FREQ] ?: defaults.equalizer.lowPassFreq
            ),
            crossfade = CrossfadeSettings(
                enabled = this[Keys.CROSSFADE_ENABLED] ?: defaults.crossfade.enabled,
                durationMs = this[Keys.CROSSFADE_DURATION_MS] ?: defaults.crossfade.durationMs
            ),
            preferHardwareDecoder = this[Keys.PREFER_HW_DECODER] ?: defaults.preferHardwareDecoder,
            lastPlayedSongId = this[Keys.LAST_PLAYED_SONG_ID],
            lastPlayedPositionMs = this[Keys.LAST_PLAYED_POSITION_MS] ?: 0L,
            lastPlayedQueueIds = queueIds,
            useDynamicTheme = this[Keys.USE_DYNAMIC_THEME] ?: defaults.useDynamicTheme,
            dynamicThemeMode = dynamicThemeMode,
            useCustomArtAlternatives = this[Keys.USE_CUSTOM_ART_ALTERNATIVES] ?: defaults.useCustomArtAlternatives,
            albumArtMode = albumArtMode,
            shuffleEnabled = this[Keys.SHUFFLE_ENABLED] ?: defaults.shuffleEnabled,
            repeatMode = this[Keys.REPEAT_MODE] ?: defaults.repeatMode,
            audioMonitorEnabled = this[Keys.AUDIO_MONITOR_ENABLED] ?: defaults.audioMonitorEnabled,
            visualizerMode = visualizerMode,
            matrixBgEnabled = matrixBgEnabled,
            usbExclusiveHqEnabled = this[Keys.USB_EXCLUSIVE_HQ_ENABLED] ?: defaults.usbExclusiveHqEnabled,
            realtimeVisualizerEnabled = this[Keys.REALTIME_VISUALIZER_ENABLED] ?: defaults.realtimeVisualizerEnabled,
            timelineStyle = timelineStyle,
            excludedFolders = excludedFolders
        )
    }
}
