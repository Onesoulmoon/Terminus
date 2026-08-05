package com.necroware.terminusplayer.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Player

/**
 * Plain bracketed ASCII transport glyphs, each in a bordered box, matching
 * the terminal "button" language elsewhere in the app. Shuffle/repeat use
 * short text labels (SHUF / RPT / RPT1) rather than arrow-style symbols —
 * "x2" and "<->" read as ambiguous rather than as shuffle/repeat at a glance.
 */
private object Glyph {
    const val PREVIOUS = "|<<"
    const val PLAY = ">"
    const val PAUSE = "||"
    const val NEXT = ">>|"
    const val SHUFFLE = "SHUF"
    const val REPEAT_OFF = "RPT"
    const val REPEAT_ONE = "RPT1"
}

@Composable
private fun GlyphButton(
    text: String,
    onClick: () -> Unit,
    color: Color,
    boxWidth: Dp,
    boxHeight: Dp,
    fontSizeSp: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(boxWidth)
            .height(boxHeight)
            .border(1.dp, color.copy(alpha = 0.5f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = fontSizeSp.sp,
            maxLines = 1,
            overflow = TextOverflow.Clip,
            color = color
        )
    }
}

/** Full-size transport row for the Now Playing screen. */
@Composable
fun FullTransportControls(
    isPlaying: Boolean,
    shuffleEnabled: Boolean,
    repeatMode: Int,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    onToggleShuffle: () -> Unit,
    onCycleRepeat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = MaterialTheme.colorScheme.primary
    val dim = MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlyphButton(Glyph.SHUFFLE, onToggleShuffle, if (shuffleEnabled) accent else dim, 56.dp, 40.dp, 11)
        GlyphButton(Glyph.PREVIOUS, onSkipPrevious, accent, 56.dp, 56.dp, 16)
        GlyphButton(if (isPlaying) Glyph.PAUSE else Glyph.PLAY, onTogglePlayPause, accent, 68.dp, 68.dp, 19)
        GlyphButton(Glyph.NEXT, onSkipNext, accent, 56.dp, 56.dp, 16)
        GlyphButton(
            if (repeatMode == Player.REPEAT_MODE_ONE) Glyph.REPEAT_ONE else Glyph.REPEAT_OFF,
            onCycleRepeat,
            if (repeatMode != Player.REPEAT_MODE_OFF) accent else dim,
            56.dp,
            40.dp,
            10
        )
    }
}

/** Compact prev/play-pause/next row for the mini player. */
@Composable
fun CompactTransportControls(
    isPlaying: Boolean,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onSkipPrevious: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = MaterialTheme.colorScheme.primary

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        GlyphButton(Glyph.PREVIOUS, onSkipPrevious, accent, 28.dp, 28.dp, 9)
        GlyphButton(if (isPlaying) Glyph.PAUSE else Glyph.PLAY, onTogglePlayPause, accent, 32.dp, 32.dp, 11)
        GlyphButton(Glyph.NEXT, onSkipNext, accent, 28.dp, 28.dp, 9)
    }
}
