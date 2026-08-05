package com.necroware.terminusplayer.data.prefs

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
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
    val PLAYBACK_ART_STYLE = stringPreferencesKey("playback_art_style")
    val LAST_PLAYED_SONG_ID = longPreferencesKey("last_played_song_id")
    val LAST_PLAYED_POSITION_MS = longPreferencesKey("last_played_position_ms")
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

    suspend fun setPlaybackArtStyle(style: PlaybackArtStyle) {
        dataStore.edit { it[Keys.PLAYBACK_ART_STYLE] = style.name }
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

    suspend fun setLastPlayed(songId: Long?, positionMs: Long) {
        dataStore.edit { prefs ->
            if (songId != null) {
                prefs[Keys.LAST_PLAYED_SONG_ID] = songId
            } else {
                prefs.remove(Keys.LAST_PLAYED_SONG_ID)
            }
            prefs[Keys.LAST_PLAYED_POSITION_MS] = positionMs
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
        val playbackArtStyle = this[Keys.PLAYBACK_ART_STYLE]?.let { runCatching { PlaybackArtStyle.valueOf(it) }.getOrNull() }
            ?: defaults.playbackArtStyle
        val eqBands = List(5) { index -> this[eqBandKey(index)] ?: 0 }

        return UserPreferences(
            themeId = themeId,
            librarySortOrder = LibrarySortOrder(sortField, sortDirection),
            equalizer = EqualizerSettings(
                enabled = this[Keys.EQ_ENABLED] ?: defaults.equalizer.enabled,
                bandGainsDb = eqBands
            ),
            crossfade = CrossfadeSettings(
                enabled = this[Keys.CROSSFADE_ENABLED] ?: defaults.crossfade.enabled,
                durationMs = this[Keys.CROSSFADE_DURATION_MS] ?: defaults.crossfade.durationMs
            ),
            preferHardwareDecoder = this[Keys.PREFER_HW_DECODER] ?: defaults.preferHardwareDecoder,
            playbackArtStyle = playbackArtStyle,
            lastPlayedSongId = this[Keys.LAST_PLAYED_SONG_ID],
            lastPlayedPositionMs = this[Keys.LAST_PLAYED_POSITION_MS] ?: 0L
        )
    }
}
