package com.necroware.terminusplayer.ui.screens.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.necroware.terminusplayer.data.model.Song
import com.necroware.terminusplayer.ui.components.SongArt
import com.necroware.terminusplayer.ui.components.TerminalBorder
import com.necroware.terminusplayer.util.toMinutesSeconds

@Composable
fun LibraryScreen(
    onSongClick: (Song, List<Song>) -> Unit,
    onAlbumClick: (String) -> Unit,
    onArtistClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    viewModel: LibraryViewModel = hiltViewModel()
) {
    var selectedTab by remember { mutableStateOf(LibraryTab.SONGS) }
    val songs by viewModel.songs.collectAsStateWithLifecycle()
    val albums by viewModel.albums.collectAsStateWithLifecycle()
    val artists by viewModel.artists.collectAsStateWithLifecycle()
    val folders by viewModel.folders.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "> LIBRARY_",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "[SEARCH]",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onSearchClick() }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            LibraryTab.entries.forEach { tab ->
                Text(
                    text = "[${tab.name}]",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (tab == selectedTab) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.clickable { selectedTab = tab }
                )
            }
        }

        when (selectedTab) {
            LibraryTab.SONGS -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(songs, key = { it.id }) { song ->
                    SongRow(song = song) { onSongClick(song, songs) }
                }
            }
            LibraryTab.ALBUMS -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(albums, key = { it.title }) { album ->
                    TerminalBorder(
                        modifier = Modifier.fillMaxWidth().clickable { onAlbumClick(album.title) }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SongArt(uriString = album.representativeUriString, size = 48.dp)
                            Column(modifier = Modifier.padding(start = 12.dp)) {
                                Text(album.title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                                Text("${album.artist} · ${album.songCount} tracks", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
            LibraryTab.ARTISTS -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(artists, key = { it.name }) { artist ->
                    TerminalBorder(
                        modifier = Modifier.fillMaxWidth().clickable { onArtistClick(artist.name) }
                    ) {
                        Column {
                            Text(artist.name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                            Text("${artist.songCount} tracks", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            LibraryTab.FOLDERS -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(folders) { folder ->
                    TerminalBorder(modifier = Modifier.fillMaxWidth()) {
                        Text(folder, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

@Composable
private fun SongRow(song: Song, onClick: () -> Unit) {
    TerminalBorder(modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SongArt(uriString = song.uriString, size = 48.dp)
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Text(
                    song.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${song.artist} · ${song.duration.toMinutesSeconds()}${if (song.isLiked) "  [L]" else ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
