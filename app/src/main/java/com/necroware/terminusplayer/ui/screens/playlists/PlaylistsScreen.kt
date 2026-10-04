package com.necroware.terminusplayer.ui.screens.playlists

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.necroware.terminusplayer.ui.components.TerminalBorder
import com.necroware.terminusplayer.ui.components.TerminalTypewriterHeader
import com.necroware.terminusplayer.ui.theme.LocalTerminalPalette
import com.necroware.terminusplayer.ui.theme.TerminalFontFamily

@Composable
fun PlaylistsScreen(
    onPlaylistClick: (PlaylistKind) -> Unit,
    onCustomPlaylistClick: (Long) -> Unit,
    viewModel: PlaylistsViewModel = hiltViewModel()
) {
    val customPlaylists by viewModel.customPlaylists.collectAsStateWithLifecycle()
    val palette = LocalTerminalPalette.current

    Column(
        modifier = Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TerminalTypewriterHeader(
            text = "> PLAYLISTS",
            fontSize = MaterialTheme.typography.headlineMedium.fontSize,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        PlaylistKind.entries.forEachIndexed { index, kind ->
            val kindColor = when (index % 4) {
                0 -> palette.primaryAccent
                1 -> palette.secondaryAccent
                2 -> palette.tertiaryAccent
                else -> palette.highlightAccent
            }

            TerminalBorder(
                modifier = Modifier.fillMaxWidth().clickable { onPlaylistClick(kind) }
            ) {
                Text(
                    text = "[ ${kind.title} ]",
                    style = MaterialTheme.typography.titleMedium,
                    color = kindColor,
                    fontWeight = FontWeight.Bold,
                    fontFamily = TerminalFontFamily
                )
            }
        }

        if (customPlaylists.isNotEmpty()) {
            Text(
                text = "IMPORTED PLAYLISTS",
                style = MaterialTheme.typography.labelSmall,
                color = palette.tertiaryAccent,
                fontWeight = FontWeight.Bold,
                fontFamily = TerminalFontFamily,
                modifier = Modifier.padding(top = 8.dp)
            )
            customPlaylists.forEachIndexed { idx, playlist ->
                val customColor = when (idx % 3) {
                    0 -> palette.secondaryAccent
                    1 -> palette.tertiaryAccent
                    else -> palette.highlightAccent
                }
                TerminalBorder(
                    modifier = Modifier.fillMaxWidth().clickable { onCustomPlaylistClick(playlist.id) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "[ ${playlist.name} ]",
                            style = MaterialTheme.typography.titleMedium,
                            color = customColor,
                            fontWeight = FontWeight.Bold,
                            fontFamily = TerminalFontFamily
                        )
                        Text(
                            text = "${playlist.songCount} tracks",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontFamily = TerminalFontFamily
                        )
                    }
                }
            }
        } else {
            Text(
                text = "[ import an m3u playlist from Settings > Library to see it here ]",
                style = MaterialTheme.typography.bodySmall,
                color = palette.tertiaryAccent.copy(alpha = 0.8f),
                fontFamily = TerminalFontFamily,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}
