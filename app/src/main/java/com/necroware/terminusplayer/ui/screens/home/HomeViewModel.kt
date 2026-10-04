package com.necroware.terminusplayer.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.necroware.terminusplayer.data.model.Song
import com.necroware.terminusplayer.data.repository.MusicRepository
import com.necroware.terminusplayer.data.repository.StatsRange
import com.necroware.terminusplayer.data.repository.StatsRepository
import com.necroware.terminusplayer.playback.AudioVisualizerHelper
import com.necroware.terminusplayer.playback.PlaybackController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

data class HomeUiState(
    val isSyncing: Boolean = true,
    val songCount: Int = 0,
    val likedCount: Int = 0,
    val weekPlays: Int = 0,
    val weekMsPlayed: Long = 0L,
    val yourMix: List<Song> = emptyList(),
    val recentlyPlayed: List<Song> = emptyList(),
    val isPlaying: Boolean = false,
    val audioLevel: Float = 0f,
    val dynamicMetricLog: String = "",
    val topArtistName: String = "Yeat"
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MusicRepository,
    private val statsRepository: StatsRepository,
    val playbackController: PlaybackController,
    private val visualizerHelper: AudioVisualizerHelper
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeAllSongs().collect { songs ->
                _uiState.value = _uiState.value.copy(
                    songCount = songs.size,
                    likedCount = songs.count { it.isLiked }
                )
            }
        }

        viewModelScope.launch {
            playbackController.state.collect { playbackState ->
                _uiState.value = _uiState.value.copy(isPlaying = playbackState.isPlaying)
            }
        }

        // Live Audio Level observation with active playback animation fallback
        viewModelScope.launch {
            launch {
                visualizerHelper.audioLevel.collect { level ->
                    if (_uiState.value.isPlaying && level > 0f) {
                        _uiState.value = _uiState.value.copy(audioLevel = level)
                    }
                }
            }

            // Continuous pulse loop when playing if raw PCM levels are quiescent
            while (isActive) {
                delay(60)
                if (_uiState.value.isPlaying) {
                    val rawLevel = visualizerHelper.audioLevel.value
                    if (rawLevel <= 0.01f) {
                        // Smooth dynamic signal simulation for VU meter
                        val simLevel = (0.35f + Random.nextFloat() * 0.5f).coerceIn(0f, 1f)
                        _uiState.value = _uiState.value.copy(audioLevel = simLevel)
                    }
                } else {
                    _uiState.value = _uiState.value.copy(audioLevel = 0f)
                }
            }
        }

        refreshLibrary()
    }

    fun refreshLibrary() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true)
            repository.syncLibrary()
            reloadMixAndRecents()
            reloadWeekSummary()
            _uiState.value = _uiState.value.copy(isSyncing = false)
        }
    }

    suspend fun getAllSongs(): List<Song> {
        return repository.observeAllSongs().first()
    }

    /** Call after a play completes elsewhere so Home reflects the latest listen. */
    fun refreshMixAndRecents() {
        viewModelScope.launch {
            reloadMixAndRecents()
            reloadWeekSummary()
        }
    }

    private suspend fun reloadMixAndRecents() {
        val mix = repository.getYourMix(limit = 25)
        val recent = repository.getRecentlyPlayed(limit = 20)
        _uiState.value = _uiState.value.copy(yourMix = mix, recentlyPlayed = recent)
    }

    private suspend fun reloadWeekSummary() {
        val summary = statsRepository.getSummary(StatsRange.WEEK)
        val insights = statsRepository.generateInsights()
        val topLog = insights.firstOrNull() ?: "METRIC // TOP_ARTIST: 'Yeat' leading the log tracking matrix index with ${summary.totalPlays} recorded playback cycles this week."
        val topArtist = summary.topArtists.firstOrNull()?.artist ?: "Yeat"

        _uiState.value = _uiState.value.copy(
            weekPlays = summary.totalPlays,
            weekMsPlayed = summary.totalMsPlayed,
            dynamicMetricLog = topLog,
            topArtistName = topArtist
        )
    }
}
