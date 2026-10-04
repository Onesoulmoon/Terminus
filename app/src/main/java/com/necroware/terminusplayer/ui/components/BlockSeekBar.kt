package com.necroware.terminusplayer.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import com.necroware.terminusplayer.ui.theme.TerminalFontFamily

/**
 * Segmented block progress bar — [████░░░░] — instead of a continuous
 * Material Slider. Discrete blocks read as "smooth" even with infrequent
 * position updates (every ~300-500ms), since there's no continuous thumb
 * visibly jumping between ticks. Tap or drag anywhere on the bar to seek.
 */
@Composable
fun BlockSeekBar(
    positionProvider: () -> Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
    segmentCount: Int = 44,
) {
    val safeDuration = durationMs.coerceAtLeast(1L)
    var barWidthPx by remember { mutableFloatStateOf(1f) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)

    fun seekToOffsetX(x: Float) {
        val fraction = (x / barWidthPx).coerceIn(0f, 1f)
        onSeek((fraction * safeDuration).toLong())
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(12.dp)
            .onSizeChanged { barWidthPx = it.width.toFloat().coerceAtLeast(1f) }
            .pointerInput(safeDuration) {
                detectTapGestures { offset -> seekToOffsetX(offset.x) }
            }
            .pointerInput(safeDuration) {
                detectDragGestures { change, _ -> seekToOffsetX(change.position.x) }
            }
    ) {
        val position = positionProvider()
        val filledSegments = ((position.toFloat() / safeDuration) * segmentCount).toInt().coerceIn(0, segmentCount)
        val segmentWidth = size.width / segmentCount

        // Use native canvas for drawing the character blocks to match the terminal aesthetic
        val paint = android.graphics.Paint().apply {
            color = primaryColor.toArgb()
            textSize = size.height
            typeface = android.graphics.Typeface.create("JetBrains Mono", android.graphics.Typeface.NORMAL)
            textAlign = android.graphics.Paint.Align.CENTER
        }

        for (i in 0 until segmentCount) {
            val isFilled = i < filledSegments
            paint.color = (if (isFilled) primaryColor else trackColor).toArgb()
            val char = if (isFilled) "█" else "░"
            val x = i * segmentWidth + (segmentWidth / 2f)
            val y = size.height / 2f - (paint.descent() + paint.ascent()) / 2f
            drawContext.canvas.nativeCanvas.drawText(char, x, y, paint)
        }
    }
}

