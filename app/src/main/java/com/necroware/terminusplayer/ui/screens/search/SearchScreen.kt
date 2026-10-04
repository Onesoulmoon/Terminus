package com.necroware.terminusplayer.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.necroware.terminusplayer.data.model.Song
import com.necroware.terminusplayer.ui.components.SongArt
import com.necroware.terminusplayer.ui.components.TerminalBorder
import com.necroware.terminusplayer.ui.theme.LocalTerminalPalette
import com.necroware.terminusplayer.ui.theme.TerminalFontFamily
import com.necroware.terminusplayer.util.safeItemClick
import com.necroware.terminusplayer.util.toMinutesSeconds

@Composable
fun SearchScreen(
    onSongClick: (Song, List<Song>) -> Unit,
    onBack: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel()
) {
    val query by viewModel.query.collectAsStateWithLifecycle()
    val results by viewModel.results.collectAsStateWithLifecycle()
    val focusRequester = androidx.compose.runtime.remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current
    val listState = rememberLazyListState()
    val palette = LocalTerminalPalette.current

    androidx.compose.runtime.LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "︿",
                style = MaterialTheme.typography.headlineMedium,
                color = palette.primaryAccent,
                modifier = Modifier.clickable { onBack() }
            )
            Text(
                text = "  SEARCH",
                style = MaterialTheme.typography.headlineMedium,
                color = palette.secondaryAccent,
                fontWeight = FontWeight.Bold,
                fontFamily = TerminalFontFamily
            )
            Text(
                text = "_",
                style = MaterialTheme.typography.headlineMedium,
                color = palette.highlightAccent,
                fontWeight = FontWeight.Bold,
                fontFamily = TerminalFontFamily
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "> ",
                style = MaterialTheme.typography.titleMedium,
                color = palette.tertiaryAccent,
                fontWeight = FontWeight.Bold,
                fontFamily = TerminalFontFamily
            )
            SearchTextField(
                query = query,
                onQueryChange = viewModel::onQueryChange,
                focusRequester = focusRequester,
                modifier = Modifier.weight(1f)
            )
            if (query.isNotEmpty()) {
                Text(
                    text = "[X]",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.highlightAccent,
                    fontWeight = FontWeight.Bold,
                    fontFamily = TerminalFontFamily,
                    modifier = Modifier.clickable { viewModel.onQueryChange("") }
                )
            }
        }

        when {
            query.isBlank() -> Text(
                text = "[ type to search by title, artist, or album ]",
                style = MaterialTheme.typography.bodySmall,
                color = palette.tertiaryAccent.copy(alpha = 0.8f),
                fontFamily = TerminalFontFamily
            )
            results.isEmpty() -> Text(
                text = "[ no matches for \"$query\" ]",
                style = MaterialTheme.typography.bodySmall,
                color = palette.highlightAccent,
                fontFamily = TerminalFontFamily
            )
            else -> LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(results, key = { it.id }) { song ->
                    SearchResultRow(song = song, listState = listState) { viewModel.playSong(song, results) }
                }
            }
        }
    }
}

@Composable
private fun SearchTextField(
    query: String,
    onQueryChange: (String) -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier
) {
    val palette = LocalTerminalPalette.current
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier.focusRequester(focusRequester),
        textStyle = MaterialTheme.typography.titleMedium.copy(
            color = palette.secondaryAccent,
            fontFamily = TerminalFontFamily,
            fontWeight = FontWeight.Bold
        ),
        cursorBrush = androidx.compose.ui.graphics.SolidColor(palette.highlightAccent),
        singleLine = true
    )
}

@Composable
private fun SearchResultRow(
    song: Song,
    listState: androidx.compose.foundation.lazy.LazyListState,
    onClick: () -> Unit
) {
    val palette = LocalTerminalPalette.current
    TerminalBorder(
        modifier = Modifier
            .fillMaxWidth()
            .safeItemClick(listState = listState, onClick = onClick)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SongArt(uriString = song.uriString, size = 48.dp)
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Text(
                    song.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = palette.primaryAccent,
                    fontWeight = FontWeight.Bold,
                    fontFamily = TerminalFontFamily,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${song.artist} · ${song.album} · ${song.duration.toMinutesSeconds()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontFamily = TerminalFontFamily,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
