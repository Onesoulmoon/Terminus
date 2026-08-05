package com.necroware.terminusplayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.necroware.terminusplayer.data.prefs.ThemePresetId
import com.necroware.terminusplayer.data.prefs.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val themeId: StateFlow<ThemePresetId> = preferencesRepository.preferences
        .map { it.themeId }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThemePresetId.TERMINAL)
}
