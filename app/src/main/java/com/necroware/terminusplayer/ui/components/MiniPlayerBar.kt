package com.necroware.terminusplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (nowPlaying.title.isBlank()) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(
                androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            )
            .clickable { onClick() }
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
            SongArt(uriString = currentSongUri.orEmpty(), size = 34.dp)
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(
                    text = nowPlaying.title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = nowPlaying.artist,
                    style = MaterialTheme.typography.labelSmall,
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
}
