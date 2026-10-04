package com.necroware.terminusplayer.ui.screens.nowplaying

import android.graphics.Bitmap
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.necroware.terminusplayer.data.prefs.AlbumArtMode
import com.necroware.terminusplayer.ui.components.TacticalAudioMonitorCard
import com.necroware.terminusplayer.ui.components.BlockSeekBar
import com.necroware.terminusplayer.ui.components.ExtendedTransportControlsRow
import com.necroware.terminusplayer.ui.components.SignalLiveHeader
import com.necroware.terminusplayer.ui.components.SongArt
import com.necroware.terminusplayer.ui.components.TerminalArtOverlay
import com.necroware.terminusplayer.ui.components.TacticalAdaptiveLyricsView
import com.necroware.terminusplayer.ui.theme.LocalTerminalPalette
import com.necroware.terminusplayer.ui.theme.TerminalFontFamily
import com.necroware.terminusplayer.lyrics.LyricLine
import com.necroware.terminusplayer.ui.components.AudioSpectrumVisualizer
import com.necroware.terminusplayer.ui.components.CrtCathodiqueAlbumArt
import com.necroware.terminusplayer.ui.components.OptimizedAsciiAlbumArt
import com.necroware.terminusplayer.ui.components.PixelatedAlbumArt
import com.necroware.terminusplayer.ui.components.TerminalMatrixBackground
import com.necroware.terminusplayer.util.estimateKbpsFromSize
import com.necroware.terminusplayer.util.sizeLabelFromBytes
import com.necroware.terminusplayer.util.toMinutesSeconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
private fun rememberSmoothPosition(
    actualPositionMs: Long,
    isPlaying: Boolean
): MutableState<Long> {
    val smooth = remember { mutableStateOf(actualPositionMs) }

    LaunchedEffect(actualPositionMs) {
        smooth.value = actualPositionMs
    }

    LaunchedEffect(isPlaying) {
        if (!isPlaying) return@LaunchedEffect
        var lastFrameNanos = withFrameNanos { it }
        while (isActive) {
            val frameNanos = withFrameNanos { it }
            val deltaMs = (frameNanos - lastFrameNanos) / 1_000_000L
            lastFrameNanos = frameNanos
            smooth.value += deltaMs
            delay(16L)
        }
    }

    return smooth
}

@Composable
private fun AlbumArtDisplay(
    uriString: String,
    useCustomArtAlternatives: Boolean,
    albumArtMode: AlbumArtMode,
    trackTitle: String,
    showLyrics: Boolean,
    lyrics: List<LyricLine>,
    isLyricsLoading: Boolean,
    currentPositionMs: Long,
    viewModel: PlaybackViewModel,
    onAlbumClick: () -> Unit
) {
    val asciiMatrix by viewModel.asciiMatrixState.collectAsStateWithLifecycle()
    val bitmap by viewModel.currentTrackBitmap.collectAsStateWithLifecycle()

    Box(
        modifier = Modifier
            .fillMaxWidth(0.88f)
            .aspectRatio(1f)
            .clickable { onAlbumClick() },
        contentAlignment = Alignment.Center
    ) {
        if (showLyrics) {
            TacticalAdaptiveLyricsView(
                lyrics = lyrics,
                currentPositionMs = currentPositionMs,
                albumArtBitmap = bitmap,
                isLoading = isLyricsLoading,
                modifier = Modifier.fillMaxSize()
            )
        } else if (!useCustomArtAlternatives) {
            SongArt(
                uriString = uriString,
                size = 320.dp,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            when (albumArtMode) {
                AlbumArtMode.PIXELATED -> {
                    PixelatedAlbumArt(
                        bitmap = bitmap,
                        pixelResolution = 128, // High density
                        borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxSize()
                    )
                }
                AlbumArtMode.ASCII -> {
                    OptimizedAsciiAlbumArt(
                        asciiMatrixProvider = { asciiMatrix },
                        gridSize = 64,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                AlbumArtMode.CRT -> {
                    CrtCathodiqueAlbumArt(
                        bitmap = bitmap,
                        trackTitle = trackTitle,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}

@Composable
fun NowPlayingScreen(viewModel: PlaybackViewModel, onCollapse: () -> Unit) {
    val nowPlaying by viewModel.nowPlaying.collectAsStateWithLifecycle()
    val positionMs by viewModel.positionMs.collectAsStateWithLifecycle()
    val isLiked by viewModel.isCurrentLiked.collectAsStateWithLifecycle()
    val currentSongUri by viewModel.currentSongUri.collectAsStateWithLifecycle()
    val audioMonitorEnabled by viewModel.audioMonitorEnabled.collectAsStateWithLifecycle()
    val useCustomArtAlternatives by viewModel.useCustomArtAlternatives.collectAsStateWithLifecycle()
    val albumArtMode by viewModel.albumArtMode.collectAsStateWithLifecycle()
    val showLyrics by viewModel.showLyrics.collectAsStateWithLifecycle()
    val lyrics by viewModel.lyricsState.collectAsStateWithLifecycle()
    val isLyricsLoading by viewModel.isLyricsLoading.collectAsStateWithLifecycle()
    val lyricsSourceTag by viewModel.lyricsSourceTag.collectAsStateWithLifecycle()
    val lyricsError by viewModel.lyricsError.collectAsStateWithLifecycle()
    val visualizerMode by viewModel.visualizerMode.collectAsStateWithLifecycle()
    val matrixBgEnabled by viewModel.matrixBgEnabled.collectAsStateWithLifecycle()
    val currentTrackBitmap by viewModel.currentTrackBitmap.collectAsStateWithLifecycle()

    val durationMs by viewModel.durationMs.collectAsStateWithLifecycle()
    val effectiveDuration = if (nowPlaying.durationMs > 0L) nowPlaying.durationMs else durationMs
    
    val smoothPositionState = rememberSmoothPosition(positionMs, nowPlaying.isPlaying)
    val smoothPositionMs = smoothPositionState.value.coerceIn(0L, effectiveDuration.coerceAtLeast(0L))

    val coroutineScope = rememberCoroutineScope()
    val offsetY = remember { Animatable(0f) }
    var screenHeightPx by remember { mutableStateOf(1f) }
    val dismissThresholdFraction = 0.25f

    val activeThemePalette = LocalTerminalPalette.current

    // Resolved bitrate in kbps (measured or estimated from container/file size)
    val resolvedKbps = if (nowPlaying.bitrateKbps > 0) {
        nowPlaying.bitrateKbps
    } else {
        estimateKbpsFromSize(nowPlaying.sizeBytes, effectiveDuration)
    }
    val kbpsText = if (resolvedKbps > 0) resolvedKbps.toString() else "—"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { screenHeightPx = it.height.toFloat().coerceAtLeast(1f) }
            .graphicsLayer {
                translationY = offsetY.value
            }
            .pointerInput(Unit) {
                detectVerticalDragGestures(
                    onVerticalDrag = { change, dragAmount ->
                        change.consume()
                        val newOffset = (offsetY.value + dragAmount).coerceAtLeast(0f)
                        coroutineScope.launch { offsetY.snapTo(newOffset) }
                    },
                    onDragEnd = {
                        coroutineScope.launch {
                            if (offsetY.value > screenHeightPx * dismissThresholdFraction) {
                                onCollapse()
                            } else {
                                offsetY.animateTo(0f)
                            }
                        }
                    }
                )
            }
    ) {
        if (matrixBgEnabled) {
            TerminalMatrixBackground(
                accentColor = activeThemePalette.primaryAccent,
                modifier = Modifier.fillMaxSize()
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "︿",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onCollapse() },
                    fontFamily = TerminalFontFamily
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (showLyrics) "[ART]" else "[LYRICS]",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { viewModel.toggleLyrics() }.padding(end = 16.dp),
                        fontFamily = TerminalFontFamily
                    )
                    Text(
                        text = if (isLiked) "[LIKED]" else "[LIKE]",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable { viewModel.toggleCurrentLike() },
                        fontFamily = TerminalFontFamily
                    )
                }
            }

            // Upgraded Multi-Mode Audio Spectrum Visualizer
            val visualizerColor = activeThemePalette.primaryAccent
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.cycleVisualizerMode() }
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "● SIGNAL // LIVE [${visualizerMode.name}]",
                        color = visualizerColor,
                        fontFamily = TerminalFontFamily,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "${nowPlaying.codecLabel}  ${nowPlaying.sampleRateLabel}",
                        color = visualizerColor.copy(alpha = 0.7f),
                        fontFamily = TerminalFontFamily,
                        fontSize = 11.sp
                    )
                }
                
                val fftData by viewModel.spectrumState.collectAsStateWithLifecycle()
                AudioSpectrumVisualizer(
                    fftData = fftData,
                    mode = visualizerMode,
                    accentColor = visualizerColor,
                    modifier = Modifier.fillMaxWidth().height(90.dp)
                )
            }

            Spacer(modifier = Modifier.weight(0.5f))

            // Album Art / CRT / ASCII Art / Lyrics Box
            AlbumArtDisplay(
                uriString = currentSongUri.orEmpty(),
                useCustomArtAlternatives = useCustomArtAlternatives,
                albumArtMode = albumArtMode,
                trackTitle = nowPlaying.title,
                showLyrics = showLyrics,
                lyrics = lyrics,
                isLyricsLoading = isLyricsLoading,
                currentPositionMs = smoothPositionMs,
                viewModel = viewModel,
                onAlbumClick = { viewModel.toggleLyrics() }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = nowPlaying.title.ifBlank { "—" },
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontFamily = TerminalFontFamily
            )
            Text(
                text = nowPlaying.artist.uppercase(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
                fontFamily = TerminalFontFamily
            )

            Spacer(modifier = Modifier.weight(0.5f))

            // Seek Bar - Defer state reading
            BlockSeekBar(
                positionProvider = { smoothPositionState.value.coerceIn(0L, effectiveDuration.coerceAtLeast(0L)) },
                durationMs = effectiveDuration,
                onSeek = { newPositionMs ->
                    smoothPositionState.value = newPositionMs
                    viewModel.seekTo(newPositionMs)
                },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(smoothPositionMs.toMinutesSeconds(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = TerminalFontFamily)
                Text(effectiveDuration.toMinutesSeconds(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = TerminalFontFamily)
            }

            // Extended ASCII Micro-animated Controls
            ExtendedTransportControlsRow(
                isPlaying = nowPlaying.isPlaying,
                isShuffleOn = nowPlaying.shuffleEnabled,
                repeatMode = nowPlaying.repeatMode,
                onTogglePlay = { viewModel.togglePlayPause() },
                onSkipPrevious = { viewModel.skipToPrevious() },
                onSkipNext = { viewModel.skipToNext() },
                onLongPressPrevious = { viewModel.seekBackward5s() },
                onLongPressNext = { viewModel.seekForward5s() },
                onToggleShuffle = { viewModel.toggleShuffle() },
                onToggleRepeat = { viewModel.cycleRepeatMode() },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Audio Monitor (Tactical Telemetry Style)
            if (audioMonitorEnabled) {
                TacticalAudioMonitorCard(
                    codecLabel = nowPlaying.codecLabel,
                    bitDepthLabel = nowPlaying.bitDepthLabel,
                    kbpsLabel = kbpsText,
                    sizeLabel = sizeLabelFromBytes(nowPlaying.sizeBytes),
                    isLossless = nowPlaying.isLossless,
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
        }
    }
}
