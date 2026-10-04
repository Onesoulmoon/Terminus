package com.necroware.terminusplayer.ui.screens.library

import android.content.IntentSender
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
import com.necroware.terminusplayer.util.UiFeedbackController
import com.necroware.terminusplayer.util.toMediaItems
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LibraryTab { SONGS, ALBUMS, ARTISTS, FOLDERS }

data class FolderNode(
    val name: String,
    val fullPath: String,
    val isDirectory: Boolean,
    val songCount: Int = 0
)

@HiltViewModel
class LibraryViewModel @Inject constructor(
    private val repository: MusicRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val playbackController: PlaybackController,
    private val uiFeedbackController: UiFeedbackController
) : ViewModel() {

    private val _selectedTab = MutableStateFlow(LibraryTab.SONGS)
    val selectedTab: StateFlow<LibraryTab> = _selectedTab

    val nowPlaying = playbackController.state

    private val _currentFolderPath = MutableStateFlow<String?>(null)
    val currentFolderPath: StateFlow<String?> = _currentFolderPath

    fun setSelectedTab(tab: LibraryTab) {
        _selectedTab.value = tab
    }

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

    val folderNodes: StateFlow<List<FolderNode>> = combine(repository.observeAllSongs(), _currentFolderPath) { allSongs, currentPath ->
        if (currentPath == null) {
            val parentMap = allSongs.groupBy { song ->
                song.folderPath.ifBlank { "Music" }
            }
            parentMap.map { (path, songsInDir) ->
                val name = path.substringAfterLast("/").ifBlank { path }
                FolderNode(name = name, fullPath = path, isDirectory = true, songCount = songsInDir.size)
            }.sortedBy { it.name.lowercase() }
        } else {
            val subFolders = allSongs.mapNotNull {
                val parent = it.folderPath
                if (parent.startsWith(currentPath) && parent != currentPath) {
                    val rel = parent.removePrefix(currentPath).removePrefix("/")
                    val firstSegment = rel.substringBefore("/")
                    if (firstSegment.isNotBlank()) "$currentPath/$firstSegment" else null
                } else null
            }.distinct()

            subFolders.map { subPath ->
                val count = allSongs.count { it.folderPath.startsWith(subPath) }
                val name = subPath.substringAfterLast("/")
                FolderNode(name = name, fullPath = subPath, isDirectory = true, songCount = count)
            }.sortedBy { it.name.lowercase() }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentFolderSongs: StateFlow<List<Song>> = combine(repository.observeAllSongs(), _currentFolderPath) { allSongs, currentPath ->
        if (currentPath == null) emptyList() else allSongs.filter { it.folderPath == currentPath }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun openFolder(path: String) {
        _currentFolderPath.value = path
    }

    fun navigateUpFolder() {
        val path = _currentFolderPath.value ?: return
        val parent = path.substringBeforeLast("/")
        _currentFolderPath.value = if (parent.contains("/")) parent else null
    }

    fun playSong(song: Song, queue: List<Song>) {
        val index = queue.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        playbackController.playSongs(queue.toMediaItems(), index)
    }

    fun toggleLike(songId: Long) {
        viewModelScope.launch { repository.toggleLike(songId) }
    }

    fun addToQueue(song: Song) {
        playbackController.addSongToQueue(song)
        uiFeedbackController.showQueued()
    }

    fun playNext(song: Song) {
        playbackController.playSongNext(song)
        uiFeedbackController.showQueued()
    }

    /** Direct-delete path used on Android 10 and below. */
    fun deleteSongDirect(song: Song) {
        viewModelScope.launch {
            repository.deleteSongDirect(song)
        }
    }

    /** Android 11+ requires a user-confirmed MediaStore delete request. */
    fun createDeleteRequest(song: Song): IntentSender? = repository.createDeleteRequest(song)

    /** Call only after the system delete confirmation returns RESULT_OK. */
    fun finalizeDeletedSong(song: Song) {
        viewModelScope.launch {
            repository.removeSongFromCache(song)
        }
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
