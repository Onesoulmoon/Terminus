package com.necroware.terminusplayer.ui.screens.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.MutatePriority
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.withFrameNanos
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
import com.necroware.terminusplayer.ui.components.safeTerminalInteraction
import com.necroware.terminusplayer.ui.components.TerminalTypewriterHeader
import com.necroware.terminusplayer.util.UiFeedbackController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onSongClick: (Song, List<Song>) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    val uiFeedbackViewModel: com.necroware.terminusplayer.util.UiFeedbackViewModel = hiltViewModel()
    val uiFeedbackController = uiFeedbackViewModel.controller

    val recentlyPlayed = state.recentlyPlayed
    val listSize = recentlyPlayed.size
    val infiniteCount = if (listSize > 1) 10000 else listSize
    val lazyListState = rememberLazyListState(
        initialFirstVisibleItemIndex = if (listSize > 1) infiniteCount / 2 else 0
    )

    if (listSize > 1) {
        LaunchedEffect(Unit) {
            while (true) {
                // Smooth continuous crawl (marquee style)
                // MutatePriority.Default allows the user to still drag/interact
                lazyListState.scroll(scrollPriority = MutatePriority.Default) {
                    var lastFrameTime = withFrameNanos { it }
                    while (true) {
                        val nextFrameTime = withFrameNanos { it }
                        val deltaNanos = nextFrameTime - lastFrameTime
                        val deltaPixels = (deltaNanos / 1_000_000_000f) * 50f // 50 pixels per second
                        scrollBy(deltaPixels)
                        lastFrameTime = nextFrameTime
                    }
                }
                // Wait briefly if interrupted by user interaction before resuming
                delay(3000)
            }
        }
    }

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
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            SystemInfoCard(
                trackCount = state.songCount,
                likedCount = state.likedCount,
                weekPlays = state.weekPlays,
                weekMsPlayed = state.weekMsPlayed,
                topArtist = state.topArtistName,
                metricLogText = state.dynamicMetricLog,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        item {
            com.necroware.terminusplayer.ui.components.TopVuMeterHeader(
                audioLevel = state.audioLevel,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }

        item {
            YourMixCard(
                mix = state.yourMix,
                onPlayMix = {
                    scope.launch {
                        val allSongs = viewModel.getAllSongs()
                        if (allSongs.isNotEmpty()) {
                            val shuffled = allSongs.shuffled()
                            onSongClick(shuffled.first(), shuffled)
                        }
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
                LazyRow(
                    state = lazyListState,
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(
                        count = infiniteCount,
                        key = { index -> index }
                    ) { index ->
                        val song = recentlyPlayed[index % listSize]
                        RecentlyPlayedChip(
                            song = song,
                            listState = lazyListState,
                            onClick = { onSongClick(song, recentlyPlayed) }
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
    val palette = com.necroware.terminusplayer.ui.theme.LocalTerminalPalette.current
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                TerminalTypewriterHeader(
                    text = "> SHUFFLE ALL",
                    fontSize = MaterialTheme.typography.displayLarge.fontSize,
                    color = palette.secondaryAccent,
                    cursorColor = palette.highlightAccent
                )
                Text(
                    text = "[ Shuffle and play your entire library ]",
                    style = MaterialTheme.typography.bodyMedium,
                    color = palette.tertiaryAccent,
                    fontFamily = com.necroware.terminusplayer.ui.theme.TerminalFontFamily
                )
            }
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

        if (mix.isNotEmpty()) {
            Text(
                text = "YOUR MIX PREVIEW",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 16.dp)
            )
            LazyRow(
                modifier = Modifier.padding(top = 8.dp),
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
private fun RecentlyPlayedChip(song: Song, listState: androidx.compose.foundation.lazy.LazyListState, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(120.dp)
            .safeTerminalInteraction(listState = listState, onClick = onClick)
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
