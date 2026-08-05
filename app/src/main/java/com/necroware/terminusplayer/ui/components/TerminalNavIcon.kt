package com.necroware.terminusplayer.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.necroware.terminusplayer.ui.navigation.Destination

/**
 * Plain typographic glyphs standing in for icons, matching the terminal
 * aesthetic — deliberately not Material icon glyphs and not emoji.
 */
private fun glyphFor(destination: Destination): String = when (destination) {
    Destination.Home -> ">_"
    Destination.Library -> "[≡]"
    Destination.Playlists -> "[▤]"
    Destination.Stats -> "[#]"
    Destination.Settings -> "[*]"
    Destination.NowPlaying -> "[▶]"
    Destination.Search -> "[?]"
    Destination.AlbumDetail -> "[○]"
    Destination.ArtistDetail -> "[&]"
    Destination.PlaylistDetail -> "[≣]"
}

@Composable
fun TerminalNavIcon(destination: Destination, tint: Color) {
    Text(
        text = glyphFor(destination),
        style = MaterialTheme.typography.titleMedium,
        color = tint
    )
}
