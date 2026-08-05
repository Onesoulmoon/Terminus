package com.necroware.terminusplayer.ui.screens.nowplaying

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.necroware.terminusplayer.data.prefs.PlaybackArtStyle
import com.necroware.terminusplayer.data.prefs.UserPreferencesRepository
import com.necroware.terminusplayer.data.repository.MusicRepository
import com.necroware.terminusplayer.playback.NowPlayingState
import com.necroware.terminusplayer.playback.PlaybackController
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlaybackViewModel @Inject constructor(
    private val controller: PlaybackController,
    private val repository: MusicRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val nowPlaying: StateFlow<NowPlayingState> = controller.state
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NowPlayingState())

    val artStyle: StateFlow<PlaybackArtStyle> = preferencesRepository.preferences
        .map { it.playbackArtStyle }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlaybackArtStyle.STANDARD)

    /**
     * Media3's MediaController doesn't preserve a MediaItem's local content
     * URI (see MusicRepository.getSongUri for why), so the current track's
     * real file URI — needed for reliable per-file album art — is resolved
     * via a repository lookup whenever the track changes, rather than read
     * directly off nowPlaying.
     */
    val currentSongUri: StateFlow<String?> = nowPlaying
        .map { it.mediaId?.toLongOrNull() }
        .distinctUntilChanged()
        .map { songId -> songId?.let { repository.getSongUri(it) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Only actively polls while something is collecting positionMs (i.e.
    // Now Playing is visible) — WhileSubscribed stops the underlying flow
    // ~5s after the last collector goes away, instead of ticking in the
    // background for the whole app session.
    val positionMs: StateFlow<Long> = flow {
        while (true) {
            emit(controller.currentPositionMs())
            delay(if (nowPlaying.value.isPlaying) 500L else 2000L)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val isCurrentLiked: StateFlow<Boolean> =
        combine(nowPlaying, repository.observeLikedIds()) { playing, likedIds ->
            val currentId = playing.mediaId?.toLongOrNull()
            currentId != null && currentId in likedIds
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        controller.connect()
    }

    fun togglePlayPause() = controller.togglePlayPause()
    fun skipToNext() = controller.skipToNext()
    fun skipToPrevious() = controller.skipToPrevious()
    fun seekTo(positionMs: Long) = controller.seekTo(positionMs)
    fun toggleShuffle() = controller.toggleShuffle()
    fun cycleRepeatMode() = controller.cycleRepeatMode()

    fun toggleCurrentLike() {
        val currentId = nowPlaying.value.mediaId?.toLongOrNull() ?: return
        viewModelScope.launch { repository.toggleLike(currentId) }
    }

    fun playQueue(items: List<androidx.media3.common.MediaItem>, startIndex: Int) {
        controller.playSongs(items, startIndex)
    }
}
