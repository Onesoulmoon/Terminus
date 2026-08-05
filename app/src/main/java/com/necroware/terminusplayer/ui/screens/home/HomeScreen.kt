package com.necroware.terminusplayer.ui.screens.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.necroware.terminusplayer.data.model.Song
import com.necroware.terminusplayer.ui.components.SongArt
import com.necroware.terminusplayer.ui.components.SystemInfoCard
import com.necroware.terminusplayer.ui.components.TerminalBorder
import kotlinx.coroutines.isActive

@Composable
fun HomeScreen(
    onSongClick: (Song, List<Song>) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.isSyncing && state.songCount == 0) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                Text(
                    text = "[ scanning library... ]",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 20.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            SystemInfoCard(
                trackCount = state.songCount,
                likedCount = state.likedCount,
                weekPlays = state.weekPlays,
                weekMsPlayed = state.weekMsPlayed,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        item {
            YourMixCard(
                mix = state.yourMix,
                onPlayMix = {
                    if (state.yourMix.isNotEmpty()) {
                        val shuffled = state.yourMix.shuffled()
                        onSongClick(shuffled.first(), shuffled)
                    }
                }
            )
        }

        if (state.recentlyPlayed.isNotEmpty()) {
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Text(
                        text = "RECENTLY PLAYED",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            item {
                val listState = rememberLazyListState()
                
                // Continuous slow scroll animation
                LaunchedEffect(state.recentlyPlayed) {
                    if (state.recentlyPlayed.size <= 1) return@LaunchedEffect
                    while (isActive) {
                        listState.scrollBy(1f) // Slow creep
                        kotlinx.coroutines.delay(16) // ~60fps
                        
                        // If reached end, jump back to start (or just let it be if not looping)
                        // For a simple "moving" effect, we can just let it scroll.
                        // Better: auto-reset if it stops moving or reaches end.
                        if (!listState.canScrollForward) {
                            listState.scrollToItem(0)
                        }
                    }
                }

                LazyRow(
                    state = listState,
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.recentlyPlayed, key = { it.id }) { song ->
                        RecentlyPlayedChip(
                            song = song,
                            onClick = { onSongClick(song, state.recentlyPlayed) }
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "[ ${state.songCount} tracks indexed ]",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
    }
}

@Composable
private fun YourMixCard(mix: List<Song>, onPlayMix: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "> YOUR MIX_",
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (mix.isEmpty()) {
                        "[ listen to a few tracks to build your mix ]"
                    } else {
                        "[ ${mix.size} tracks · built from your listening ]"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (mix.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .clickable { onPlayMix() },
                    contentAlignment = Alignment.Center
                ) {
                    TerminalBorder {
                        Text(
                            text = "▶",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        if (mix.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(mix.take(10), key = { it.id }) { song ->
                    SongArt(uriString = song.uriString, size = 96.dp)
                }
            }
        }
    }
}

@Composable
private fun RecentlyPlayedChip(song: Song, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .clickable { onClick() }
    ) {
        SongArt(uriString = song.uriString, size = 120.dp)
        Text(
            text = song.title,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 6.dp)
        )
        Text(
            text = song.artist,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
