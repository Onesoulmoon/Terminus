package com.necroware.terminusplayer.ui.screens.playlists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.necroware.terminusplayer.data.model.Song
import com.necroware.terminusplayer.ui.components.SongArt
import com.necroware.terminusplayer.ui.theme.TerminalFontFamily
import com.necroware.terminusplayer.util.safeItemClick
import com.necroware.terminusplayer.util.toMinutesSeconds

@Composable
fun PlaylistDetailScreen(
    onBack: () -> Unit,
    viewModel: PlaylistDetailViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "︿",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { onBack() }
                )
                if (viewModel.isCustomPlaylist) {
                    Text(
                        text = "[ DELETE PLAYLIST ]",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                        fontFamily = TerminalFontFamily,
                        modifier = Modifier.clickable {
                            viewModel.deletePlaylist(onDeleted = onBack)
                        }
                    )
                }
            }
        }

        item {
            Column(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Text(
                    text = "[ ${state.title} ]",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "[ ${state.songs.size} tracks ]",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        item {
            Text(
                text = "▶ PLAY ALL",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .clickable { viewModel.playAll() }
            )
        }

        if (!state.isLoading && state.songs.isEmpty()) {
            item {
                Text(
                    text = state.emptyMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(state.songs, key = { it.id }) { song ->
            PlaylistTrackRow(
                song = song,
                isCustom = viewModel.isCustomPlaylist,
                listState = listState,
                onClick = { viewModel.playFrom(song) },
                onRemove = { viewModel.removeSong(song) }
            )
        }
    }
}

@Composable
private fun PlaylistTrackRow(
    song: Song,
    isCustom: Boolean,
    listState: LazyListState,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .safeItemClick(listState = listState, onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SongArt(uriString = song.uriString, size = 44.dp)
        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
            Text(
                text = song.title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = song.artist,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = song.duration.toMinutesSeconds(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (isCustom) {
                Text(
                    text = "[ X ]",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error,
                    fontFamily = TerminalFontFamily,
                    modifier = Modifier.clickable { onRemove() }
                )
            }
        }
    }
}
