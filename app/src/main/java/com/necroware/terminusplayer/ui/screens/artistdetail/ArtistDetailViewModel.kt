package com.necroware.terminusplayer.ui.screens.artistdetail

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

data class ArtistDetailUiState(
    val isLoading: Boolean = true,
    val artist: String = "",
    val songs: List<Song> = emptyList(),
    val stats: GroupListenStats = GroupListenStats(0, 0L, null),
    val topSongTitle: String? = null
)

@HiltViewModel
class ArtistDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: MusicRepository,
    private val statsRepository: StatsRepository,
    private val playbackController: PlaybackController
) : ViewModel() {

    private val artist: String = checkNotNull(savedStateHandle["artist"])

    private val _uiState = MutableStateFlow(ArtistDetailUiState(artist = artist))
    val uiState: StateFlow<ArtistDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val songs = repository.getSongsForArtist(artist)
            val stats = statsRepository.getStatsForArtist(artist)
            val topSongTitle = stats.topSongId?.let { id -> songs.firstOrNull { it.id == id }?.title }

            _uiState.value = ArtistDetailUiState(
                isLoading = false,
                artist = artist,
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
