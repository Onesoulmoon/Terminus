package com.necroware.terminusplayer.ui.screens.albumdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.necroware.terminusplayer.data.model.Song
import com.necroware.terminusplayer.data.repository.GroupListenStats
import com.necroware.terminusplayer.data.repository.MusicRepository
import com.necroware.terminusplayer.data.repository.StatsRepository
import com.necroware.terminusplayer.playback.PlaybackController
import com.necroware.terminusplayer.util.toMediaItems
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AlbumDetailUiState(
    val isLoading: Boolean = true,
    val albumId: Long = -1L,
    val representativeUriString: String = "",
    val albumTitle: String = "",
    val artist: String = "",
    val songs: List<Song> = emptyList(),
    val stats: GroupListenStats = GroupListenStats(0, 0L, null),
    val topSongTitle: String? = null
)

@HiltViewModel
class AlbumDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MusicRepository,
    private val statsRepository: StatsRepository,
    private val playbackController: PlaybackController
) : ViewModel() {

    // Keyed by TITLE, not MediaStore albumId — see Destination.AlbumDetail
    // for why (MediaStore fragments one album across several albumIds).
    private val albumTitle: String = checkNotNull(savedStateHandle["albumTitle"])

    private val _uiState = MutableStateFlow(AlbumDetailUiState(albumTitle = albumTitle))
    val uiState: StateFlow<AlbumDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val songs = repository.getSongsForAlbum(albumTitle)
            val stats = statsRepository.getStatsForAlbum(albumTitle)
            val topSongTitle = stats.topSongId?.let { id -> songs.firstOrNull { it.id == id }?.title }

            _uiState.value = AlbumDetailUiState(
                isLoading = false,
                // Representative albumId (first song's) purely for legacy
                // reference — actual art now loads per-file via
                // representativeUriString (see SongArt.kt for why).
                albumId = songs.firstOrNull()?.albumId ?: -1L,
                representativeUriString = songs.firstOrNull()?.uriString.orEmpty(),
                albumTitle = songs.firstOrNull()?.album ?: albumTitle,
                artist = songs.firstOrNull()?.artist.orEmpty(),
                songs = songs,
                stats = stats,
                topSongTitle = topSongTitle
            )
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
