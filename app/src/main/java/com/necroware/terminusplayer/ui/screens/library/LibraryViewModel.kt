package com.necroware.terminusplayer.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.necroware.terminusplayer.data.model.Album
import com.necroware.terminusplayer.data.model.Artist
import com.necroware.terminusplayer.data.model.Song
import com.necroware.terminusplayer.data.prefs.LibrarySortOrder
import com.necroware.terminusplayer.data.prefs.SortDirection
import com.necroware.terminusplayer.data.prefs.SortField
import com.necroware.terminusplayer.data.prefs.UserPreferencesRepository
import com.necroware.terminusplayer.data.repository.MusicRepository
import com.necroware.terminusplayer.playback.PlaybackController
import com.necroware.terminusplayer.util.toMediaItems
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LibraryTab { SONGS, ALBUMS, ARTISTS, FOLDERS }

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: MusicRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val playbackController: PlaybackController
) : ViewModel() {

    private val sortOrder: StateFlow<LibrarySortOrder> = preferencesRepository.preferences
        .map { it.librarySortOrder }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LibrarySortOrder())

    val songs: StateFlow<List<Song>> =
        combine(repository.observeAllSongs(), sortOrder) { songs, order -> songs.sortedWith(order) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val albums: StateFlow<List<Album>> = repository.observeAllAlbums()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val artists: StateFlow<List<Artist>> = repository.observeAllArtists()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val folders: StateFlow<List<String>> = repository.observeAllFolders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun playSong(song: Song, queue: List<Song>) {
        val index = queue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        playbackController.playSongs(queue.toMediaItems(), index)
    }

    fun toggleLike(songId: Long) {
        viewModelScope.launch { repository.toggleLike(songId) }
    }
}

private fun List<Song>.sortedWith(order: LibrarySortOrder): List<Song> {
    val comparator: Comparator<Song> = when (order.field) {
        SortField.TITLE -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.title }
        SortField.ARTIST -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.artist }
        SortField.ALBUM -> compareBy(String.CASE_INSENSITIVE_ORDER) { it.album }
        SortField.DATE_ADDED -> compareBy { it.dateAdded }
        SortField.DURATION -> compareBy { it.duration }
    }
    val sorted = sortedWith(comparator)
    return if (order.direction == SortDirection.DESC) sorted.asReversed() else sorted
}
