package com.necroware.terminusplayer.ui.screens.nowplaying

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.necroware.terminusplayer.data.prefs.UserPreferencesRepository
import com.necroware.terminusplayer.data.repository.MusicRepository
import com.necroware.terminusplayer.data.repository.TrackMetadataRepository
import com.necroware.terminusplayer.playback.NowPlayingState
import com.necroware.terminusplayer.playback.PlaybackController
import com.necroware.terminusplayer.util.FastAsciiMatrix
import com.necroware.terminusplayer.lyrics.LocalLrcReader
import com.necroware.terminusplayer.lyrics.LrcParser
import com.necroware.terminusplayer.lyrics.LrclibFetcher
import com.necroware.terminusplayer.lyrics.LyricLine
import com.necroware.terminusplayer.lyrics.LyricRepository
import com.necroware.terminusplayer.lyrics.LyricResult
import com.necroware.terminusplayer.lyrics.LyricSanitizer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class PlaybackViewModel @Inject constructor(
    private val controller: PlaybackController,
    private val repository: MusicRepository,
    private val metadataRepository: TrackMetadataRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val visualizerHelper: com.necroware.terminusplayer.playback.AudioVisualizerHelper
) : ViewModel() {

    val nowPlaying: StateFlow<NowPlayingState> = controller.state
        .onEach { state ->
            if (state.mediaId != _lastMediaId) {
                _lastMediaId = state.mediaId
                _lyricsState.value = emptyList()
                _lyricsError.value = null
                _lyricsSourceTag.value = null
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NowPlayingState())

    private var _lastMediaId: String? = null

    val spectrumState: StateFlow<FloatArray> = visualizerHelper.barAmplitudes

    private val _currentTrackBitmap = MutableStateFlow<Bitmap?>(null)
    val currentTrackBitmap: StateFlow<Bitmap?> = _currentTrackBitmap

    val asciiMatrixState: StateFlow<FastAsciiMatrix?> = _currentTrackBitmap
        .filterNotNull()
        .map { bitmap ->
            withContext(Dispatchers.Default) {
                FastAsciiMatrix(64).apply { updateFromBitmap(bitmap) }
            }
        }
        .flowOn(Dispatchers.Default)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun updateTrackBitmap(bitmap: Bitmap?) {
        _currentTrackBitmap.value = bitmap
    }

    val audioMonitorEnabled: StateFlow<Boolean> = preferencesRepository.preferences
        .map { it.audioMonitorEnabled }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val useCustomArtAlternatives: StateFlow<Boolean> = preferencesRepository.preferences
        .map { it.useCustomArtAlternatives }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val albumArtMode: StateFlow<com.necroware.terminusplayer.data.prefs.AlbumArtMode> = preferencesRepository.preferences
        .map { it.albumArtMode }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.necroware.terminusplayer.data.prefs.AlbumArtMode.PIXELATED)

    val visualizerMode: StateFlow<com.necroware.terminusplayer.data.prefs.VisualizerMode> = preferencesRepository.preferences
        .map { it.visualizerMode }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), com.necroware.terminusplayer.data.prefs.VisualizerMode.BARS)

    val matrixBgEnabled: StateFlow<Boolean> = preferencesRepository.preferences
        .map { it.matrixBgEnabled }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _showLyrics = MutableStateFlow(false)
    val showLyrics: StateFlow<Boolean> = _showLyrics

    private val _lyricsState = MutableStateFlow<List<LyricLine>>(emptyList())
    val lyricsState: StateFlow<List<LyricLine>> = _lyricsState

    private val _lyricsSourceTag = MutableStateFlow<String?>(null)
    val lyricsSourceTag: StateFlow<String?> = _lyricsSourceTag

    private val _lyricsError = MutableStateFlow<String?>(null)
    val lyricsError: StateFlow<String?> = _lyricsError

    private val _isLyricsLoading = MutableStateFlow(false)
    val isLyricsLoading: StateFlow<Boolean> = _isLyricsLoading

    private val _lyricsTextSizeSp = MutableStateFlow(16)
    val lyricsTextSizeSp: StateFlow<Int> = _lyricsTextSizeSp

    fun increaseLyricsTextSize() {
        if (_lyricsTextSizeSp.value < 28) _lyricsTextSizeSp.value += 2
    }

    fun decreaseLyricsTextSize() {
        if (_lyricsTextSizeSp.value > 10) _lyricsTextSizeSp.value -= 2
    }

    fun toggleLyrics() {
        _showLyrics.value = !_showLyrics.value
        if (_showLyrics.value && _lyricsState.value.isEmpty()) {
            fetchLyrics()
        }
    }

    private fun fetchLyrics() {
        val state = nowPlaying.value
        val uri = currentSongUri.value
        fetchLyricsFor(uri, state.title, state.artist, state.mediaId)
    }

    private fun fetchLyricsFor(audioUri: String?, title: String, artist: String, mediaId: String?) {
        if (title.isBlank()) return

        viewModelScope.launch {
            _isLyricsLoading.value = true
            _lyricsError.value = null
            _lyricsState.value = emptyList() // Clear immediately
            _lyricsSourceTag.value = null
            
            val result = LyricRepository.getLyrics(
                audioPath = audioUri,
                trackTitle = title,
                artistName = artist
            )
            
            if (nowPlaying.value.mediaId == mediaId) {
                when (result) {
                    is LyricResult.Success -> {
                        _lyricsState.value = result.lines
                        _lyricsSourceTag.value = result.sourceTag
                    }
                    is LyricResult.Error -> {
                        _lyricsError.value = result.message
                    }
                }
            }
            _isLyricsLoading.value = false
        }
    }

    /**
     * Media3's MediaController doesn't preserve a MediaItem's local content
     * URI (see MusicRepository.getSongUri for why), so the current track's
     * real file URI — needed for reliable per-file album art — is resolved
     * via a repository lookup whenever the track changes, rather than read
     * directly off nowPlaying.
     */
    val currentSongUri: StateFlow<String?> = nowPlaying
        .transformLatest { state ->
            val mediaId = state.mediaId
            emit(when {
                mediaId == null -> null
                mediaId.toLongOrNull() != null -> repository.getSongUri(mediaId.toLong())
                else -> mediaId
            })
        }
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Only actively polls while something is collecting positionMs (i.e.
    // Now Playing is visible) — WhileSubscribed stops the underlying flow
    // ~5s after the last collector goes away, instead of ticking in the
    // background for the whole app session.
    val positionMs: StateFlow<Long> = flow {
        while (true) {
            emit(controller.currentPositionMs())
            delay(if (nowPlaying.value.isPlaying) 200L else 1000L)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val durationMs: StateFlow<Long> = flow {
        while (true) {
            emit(controller.durationMs())
            delay(250L)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val isCurrentLiked: StateFlow<Boolean> =
        combine(nowPlaying, repository.observeLikedIds()) { playing, likedIds ->
            val currentId = playing.mediaId?.toLongOrNull()
            currentId != null && currentId in likedIds
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        controller.connect()
        
        viewModelScope.launch {
            currentSongUri.collect { uriString ->
                if (uriString != null) {
                    val metadata = metadataRepository.loadTrackMetadata(android.net.Uri.parse(uriString))
                    _currentTrackBitmap.value = metadata.artwork
                } else {
                    _currentTrackBitmap.value = null
                }
            }
        }

        viewModelScope.launch {
            combine(nowPlaying, currentSongUri, _showLyrics) { state, uri, show ->
                Triple(state, uri, show)
            }.collect { (state, uri, show) ->
                if (show && state.mediaId != null && state.title.isNotBlank() && _lyricsState.value.isEmpty() && !_isLyricsLoading.value) {
                    fetchLyricsFor(uri, state.title, state.artist, state.mediaId)
                }
            }
        }
    }

    fun togglePlayPause() = controller.togglePlayPause()
    fun skipToNext() = controller.skipToNext()
    fun skipToPrevious() = controller.skipToPrevious()
    fun seekTo(positionMs: Long) = controller.seekTo(positionMs)
    fun seekForward5s() = controller.seekForward5s()
    fun seekBackward5s() = controller.seekBackward5s()
    fun toggleShuffle() = controller.toggleShuffle()
    fun cycleRepeatMode() = controller.cycleRepeatMode()

    fun toggleCurrentLike() {
        val currentId = nowPlaying.value.mediaId?.toLongOrNull() ?: return
        viewModelScope.launch { repository.toggleLike(currentId) }
    }

    fun cycleVisualizerMode() {
        viewModelScope.launch {
            val current = visualizerMode.value
            val next = com.necroware.terminusplayer.data.prefs.VisualizerMode.entries[
                (current.ordinal + 1) % com.necroware.terminusplayer.data.prefs.VisualizerMode.entries.size
            ]
            preferencesRepository.setVisualizerMode(next)
        }
    }

    fun setMatrixBgEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setMatrixBgEnabled(enabled)
        }
    }

    fun playQueue(items: List<androidx.media3.common.MediaItem>, startIndex: Int) {
        controller.playSongs(items, startIndex)
    }
}
