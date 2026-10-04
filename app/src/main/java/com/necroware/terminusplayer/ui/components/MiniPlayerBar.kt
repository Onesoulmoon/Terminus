package com.necroware.terminusplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.necroware.terminusplayer.playback.NowPlayingState

/**
 * Edge-to-edge mini player bar sitting directly above the bottom nav — NOT
 * a floating rounded pill (that clipped the transport buttons and looked
 * broken). Plain rectangular surface, thin top border to separate it from
 * content above.
 */
@Composable
fun MiniPlayerBar(
    nowPlaying: NowPlayingState,
    currentSongUri: String?,
    positionProvider: () -> Long,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (nowPlaying.title.isBlank()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .clickable { onClick() }
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                SongArt(uriString = currentSongUri.orEmpty(), size = 42.dp)
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(
                        text = nowPlaying.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = nowPlaying.artist,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            CompactTransportControls(
                isPlaying = nowPlaying.isPlaying,
                onTogglePlayPause = onTogglePlayPause,
                onSkipNext = onSkipNext,
                onSkipPrevious = onSkipPrevious
            )
        }
        
        // Interactive mini progress bar
        val duration = nowPlaying.durationMs.coerceAtLeast(1L)
        val position = positionProvider()
        
        var scrubbingPosition by remember { mutableStateOf<Long?>(null) }
        val displayProgress = (scrubbingPosition ?: position).toFloat() / duration
        
        var barWidthPx by remember { mutableStateOf(1f) }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp) // Slightly taller for better touch target
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                .onSizeChanged { barWidthPx = it.width.toFloat().coerceAtLeast(1f) }
                .pointerInput(duration) {
                    detectTapGestures { offset ->
                        val fraction = (offset.x / barWidthPx).coerceIn(0f, 1f)
                        onSeek((fraction * duration).toLong())
                    }
                }
                .pointerInput(duration) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val fraction = (offset.x / barWidthPx).coerceIn(0f, 1f)
                            scrubbingPosition = (fraction * duration).toLong()
                        },
                        onDragEnd = {
                            scrubbingPosition?.let(onSeek)
                            scrubbingPosition = null
                        },
                        onDragCancel = {
                            scrubbingPosition = null
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val fraction = (change.position.x / barWidthPx).coerceIn(0f, 1f)
                            scrubbingPosition = (fraction * duration).toLong()
                        }
                    )
                }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(displayProgress.coerceIn(0f, 1f))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }
    }
}
