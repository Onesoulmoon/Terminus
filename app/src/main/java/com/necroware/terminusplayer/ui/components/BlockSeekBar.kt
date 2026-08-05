package com.necroware.terminusplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp

/**
 * Segmented block progress bar — [████░░░░] — instead of a continuous
 * Material Slider. Discrete blocks read as "smooth" even with infrequent
 * position updates (every ~300-500ms), since there's no continuous thumb
 * visibly jumping between ticks. Tap or drag anywhere on the bar to seek.
 */
@Composable
fun BlockSeekBar(
    positionMs: Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
    segmentCount: Int = 44
) {
    val safeDuration = durationMs.coerceAtLeast(1L)
    val filledSegments = if (durationMs <= 0L) {
        0
    } else {
        ((positionMs.toFloat() / safeDuration) * segmentCount).toInt().coerceIn(0, segmentCount)
    }

    var barWidthPx by remember { mutableStateOf(1f) }

    fun seekToOffsetX(x: Float) {
        val fraction = (x / barWidthPx).coerceIn(0f, 1f)
        onSeek((fraction * safeDuration).toLong())
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(10.dp)
            .onSizeChanged { barWidthPx = it.width.toFloat().coerceAtLeast(1f) }
            .pointerInput(safeDuration) {
                detectTapGestures { offset -> seekToOffsetX(offset.x) }
            }
            .pointerInput(safeDuration) {
                detectDragGestures { change, _ -> seekToOffsetX(change.position.x) }
            },
        horizontalArrangement = Arrangement.spacedBy(1.5.dp)
    ) {
        repeat(segmentCount) { index ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(
                        if (index < filledSegments) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
            )
        }
    }
}
