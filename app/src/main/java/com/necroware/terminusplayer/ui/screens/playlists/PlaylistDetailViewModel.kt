package com.necroware.terminusplayer.ui.screens.playlists

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.necroware.terminusplayer.data.model.Song
import com.necroware.terminusplayer.data.repository.MusicRepository
import com.necroware.terminusplayer.playback.PlaybackController
import com.necroware.terminusplayer.util.toMediaItems
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Route arg is either a [PlaylistKind] name (LIKED/RECENT/MOST_PLAYED) or
 *  "custom:<id>" for a user-imported playlist — see [Destination.PlaylistDetail]. */
data class PlaylistDetailUiState(
    val isLoading: Boolean = true,
    val kind: PlaylistKind? = null,
    val title: String = "",
    val emptyMessage: String = "",
    val songs: List<Song> = emptyList()
)

@HiltViewModel
class PlaylistDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MusicRepository,
    private val playbackController: PlaybackController
) : ViewModel() {

    private val rawArg: String = checkNotNull(savedStateHandle["kind"])
    private val customPlaylistId: Long? = rawArg.removePrefix("custom:").toLongOrNull()
        .takeIf { rawArg.startsWith("custom:") }
    private val kind: PlaylistKind? = if (customPlaylistId == null) PlaylistKind.valueOf(rawArg) else null

    private val _uiState = MutableStateFlow(PlaylistDetailUiState(kind = kind))
    val uiState: StateFlow<PlaylistDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val playlistId = customPlaylistId
            if (playlistId != null) {
                val songs = repository.getSongsForPlaylist(playlistId)
                val name = repository.getPlaylistName(playlistId)
                _uiState.value = PlaylistDetailUiState(
                    isLoading = false,
                    kind = null,
                    title = name,
                    emptyMessage = "[ nothing matched when this was imported ]",
                    songs = songs
                )
            } else {
                val resolvedKind = checkNotNull(kind)
                val songs = when (resolvedKind) {
                    PlaylistKind.LIKED -> repository.getLikedSongs()
                    PlaylistKind.RECENT -> repository.getRecentlyPlayed(limit = 100)
                    PlaylistKind.MOST_PLAYED -> repository.getMostPlayed(limit = 100)
                }
                _uiState.value = PlaylistDetailUiState(
                    isLoading = false,
                    kind = resolvedKind,
                    title = resolvedKind.title,
                    emptyMessage = when (resolvedKind) {
                        PlaylistKind.LIKED -> "[ nothing liked yet — tap the heart on Now Playing ]"
                        PlaylistKind.RECENT -> "[ nothing played yet ]"
                        PlaylistKind.MOST_PLAYED -> "[ nothing played yet ]"
                    },
                    songs = songs
                )
            }
        }
    }

    fun playAll() {
        val songs = _uiState.value.songs
        if (songs.isNotEmpty()) playbackController.playSongs(songs.toMediaItems(), 0)
    }

    fun playFrom(song: Song) {
        val songs = _uiState.value.songs
        val index = songs.indexOfFirst { it.id == song.id }.coerceAtLeast(0)
        playbackController.playSongs(songs.toMediaItems(), index)
    }
}
