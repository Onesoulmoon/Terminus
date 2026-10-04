package com.necroware.terminusplayer.ui.screens.settings

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.necroware.terminusplayer.data.prefs.AlbumArtMode
import com.necroware.terminusplayer.data.prefs.DynamicThemeMode
import com.necroware.terminusplayer.data.prefs.LibrarySortOrder
import com.necroware.terminusplayer.data.prefs.SortDirection
import com.necroware.terminusplayer.data.prefs.SortField
import com.necroware.terminusplayer.data.prefs.ThemePresetId
import com.necroware.terminusplayer.data.prefs.UserPreferences
import com.necroware.terminusplayer.data.prefs.UserPreferencesRepository
import com.necroware.terminusplayer.data.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface ImportStatus {
    data object Idle : ImportStatus
    data object Running : ImportStatus
    data class FilesDone(val count: Int) : ImportStatus
    data class PlaylistDone(val matched: Int, val total: Int) : ImportStatus
    data class Failed(val message: String) : ImportStatus
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: MusicRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val preferences: StateFlow<UserPreferences> = preferencesRepository.preferences
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPreferences())

    private val _importStatus = MutableStateFlow<ImportStatus>(ImportStatus.Idle)
    val importStatus: StateFlow<ImportStatus> = _importStatus.asStateFlow()

    fun setTheme(id: ThemePresetId) = viewModelScope.launch { preferencesRepository.setTheme(id) }

    fun setUseDynamicTheme(enabled: Boolean) = viewModelScope.launch {
        preferencesRepository.setUseDynamicTheme(enabled)
    }

    fun setDynamicThemeMode(mode: DynamicThemeMode) = viewModelScope.launch {
        preferencesRepository.setDynamicThemeMode(mode)
    }

    fun setUseCustomArtAlternatives(enabled: Boolean) = viewModelScope.launch {
        preferencesRepository.setUseCustomArtAlternatives(enabled)
    }

    fun setAlbumArtMode(mode: AlbumArtMode) = viewModelScope.launch {
        preferencesRepository.setAlbumArtMode(mode)
    }

    fun setVisualizerMode(mode: com.necroware.terminusplayer.data.prefs.VisualizerMode) = viewModelScope.launch {
        preferencesRepository.setVisualizerMode(mode)
    }

    fun setMatrixBgEnabled(enabled: Boolean) = viewModelScope.launch {
        preferencesRepository.setMatrixBgEnabled(enabled)
    }

    fun setSortField(field: SortField) = viewModelScope.launch {
        preferencesRepository.setSortOrder(preferences.value.librarySortOrder.copy(field = field))
    }

    fun setSortDirection(direction: SortDirection) = viewModelScope.launch {
        preferencesRepository.setSortOrder(preferences.value.librarySortOrder.copy(direction = direction))
    }

    fun setEqualizerEnabled(enabled: Boolean) = viewModelScope.launch {
        preferencesRepository.setEqualizerEnabled(enabled)
    }

    fun setEqualizerBand(index: Int, gainDb: Int) = viewModelScope.launch {
        preferencesRepository.setEqualizerBand(index, gainDb)
    }

    fun setPreampGain(gain: Float) = viewModelScope.launch {
        preferencesRepository.setPreampGain(gain)
    }

    fun setHighPassEnabled(enabled: Boolean) = viewModelScope.launch {
        preferencesRepository.setHighPassEnabled(enabled)
    }

    fun setHighPassFreq(freq: Float) = viewModelScope.launch {
        preferencesRepository.setHighPassFreq(freq)
    }

    fun setLowPassEnabled(enabled: Boolean) = viewModelScope.launch {
        preferencesRepository.setLowPassEnabled(enabled)
    }

    fun setLowPassFreq(freq: Float) = viewModelScope.launch {
        preferencesRepository.setLowPassFreq(freq)
    }

    fun resetEqualizerBands() = viewModelScope.launch {
        preferencesRepository.setEqualizerBands(List(10) { 0 })
    }

    fun setCrossfadeEnabled(enabled: Boolean) = viewModelScope.launch {
        preferencesRepository.setCrossfadeEnabled(enabled)
    }

    fun setCrossfadeDurationMs(durationMs: Int) = viewModelScope.launch {
        preferencesRepository.setCrossfadeDurationMs(durationMs)
    }

    fun setPreferHardwareDecoder(enabled: Boolean) = viewModelScope.launch {
        preferencesRepository.setPreferHardwareDecoder(enabled)
    }

    fun setUsbExclusiveHqEnabled(enabled: Boolean) = viewModelScope.launch {
        preferencesRepository.setUsbExclusiveHqEnabled(enabled)
    }

    fun setRealtimeVisualizerEnabled(enabled: Boolean) = viewModelScope.launch {
        preferencesRepository.setRealtimeVisualizerEnabled(enabled)
    }

    fun setAudioMonitorEnabled(enabled: Boolean) = viewModelScope.launch {
        preferencesRepository.setAudioMonitorEnabled(enabled)
    }

    fun importFiles(uris: List<Uri>) {
        if (uris.isEmpty()) return
        _importStatus.value = ImportStatus.Running
        viewModelScope.launch {
            val count = runCatching { repository.importAudioFiles(uris) }
                .getOrElse {
                    _importStatus.value = ImportStatus.Failed("Couldn't import those files")
                    return@launch
                }
            _importStatus.value = ImportStatus.FilesDone(count)
        }
    }

    fun importPlaylist(uri: Uri) {
        _importStatus.value = ImportStatus.Running
        viewModelScope.launch {
            val name = displayNameFor(uri).substringBeforeLast('.').ifBlank { "IMPORTED PLAYLIST" }
            val (matched, total) = runCatching { repository.importPlaylistFromM3u(uri, name) }
                .getOrElse {
                    _importStatus.value = ImportStatus.Failed("Couldn't read that playlist file")
                    return@launch
                }
            _importStatus.value = ImportStatus.PlaylistDone(matched, total)
        }
    }

    fun dismissImportStatus() {
        _importStatus.value = ImportStatus.Idle
    }

    private fun displayNameFor(uri: Uri): String {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) return cursor.getString(idx) ?: ""
            }
        }
        return uri.lastPathSegment.orEmpty()
    }
}
