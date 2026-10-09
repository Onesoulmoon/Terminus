package com.necroware.terminusplayer.ui.screens.playlists

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.necroware.terminusplayer.data.model.Playlist
import com.necroware.terminusplayer.data.repository.MusicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaylistsViewModel @Inject constructor(
    private val repository: MusicRepository
) : ViewModel() {

    val customPlaylists: StateFlow<List<Playlist>> = repository.observeCustomPlaylists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createPlaylist(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            repository.createCustomPlaylist(name)
        }
    }
}
