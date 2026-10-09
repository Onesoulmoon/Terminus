package com.necroware.terminusplayer.ui.components

import android.graphics.Paint
import android.graphics.Typeface
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import com.necroware.terminusplayer.data.prefs.TimelineStyle
import com.necroware.terminusplayer.ui.theme.LocalTerminalPalette

/**
 * Customizable ASCII timeline seekbar with 3 selectable styles:
 * - HIGHLIGHTED_BLOCKS: [█████░░░░] with highlighted active playhead tip
 * - ARROW_RAIL: [=======>........]
 * - ADAPTIVE_RAIL: [ooooo*-------] progressive morphing rail (- -> * -> o)
 * Tap or drag anywhere on the bar to seek when [interactive] is true.
 */
@Composable
fun BlockSeekBar(
    positionProvider: () -> Long,
    durationMs: Long,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
    style: TimelineStyle = TimelineStyle.HIGHLIGHTED_BLOCKS,
    segmentCount: Int = 40,
    tipColor: Color = LocalTerminalPalette.current.highlightAccent,
    interactive: Boolean = true
) {
    val safeDuration = durationMs.coerceAtLeast(1L)
    var barWidthPx by remember { mutableFloatStateOf(1f) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)

    fun seekToOffsetX(x: Float) {
        val fraction = (x / barWidthPx).coerceIn(0f, 1f)
        onSeek((fraction * safeDuration).toLong())
    }

    val canvasModifier = if (interactive) {
        modifier
            .fillMaxWidth()
            .height(14.dp)
            .onSizeChanged { barWidthPx = it.width.toFloat().coerceAtLeast(1f) }
            .pointerInput(safeDuration) {
                detectTapGestures { offset -> seekToOffsetX(offset.x) }
            }
            .pointerInput(safeDuration) {
                detectDragGestures { change, _ -> seekToOffsetX(change.position.x) }
            }
    } else {
        modifier
            .fillMaxWidth()
            .height(14.dp)
            .onSizeChanged { barWidthPx = it.width.toFloat().coerceAtLeast(1f) }
    }

    Canvas(modifier = canvasModifier) {
        val position = positionProvider()
        val progressFraction = (position.toFloat() / safeDuration).coerceIn(0f, 1f)

        val paint = Paint().apply {
            textSize = size.height * 0.95f
            typeface = Typeface.create("JetBrains Mono", Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        when (style) {
            TimelineStyle.HIGHLIGHTED_BLOCKS -> {
                val filledSegments = (progressFraction * segmentCount).toInt().coerceIn(0, segmentCount)
                val segmentWidth = size.width / segmentCount

                for (i in 0 until segmentCount) {
                    val isTip = (i == filledSegments - 1 && filledSegments > 0)
                    val isFilled = (i < filledSegments)
                    val char = if (isFilled) "█" else "░"

                    paint.color = when {
                        isTip -> tipColor.toArgb()
                        isFilled -> primaryColor.toArgb()
                        else -> trackColor.toArgb()
                    }

                    val x = i * segmentWidth + (segmentWidth / 2f)
                    val y = size.height / 2f - (paint.descent() + paint.ascent()) / 2f
                    drawContext.canvas.nativeCanvas.drawText(char, x, y, paint)
                }
            }

            TimelineStyle.ARROW_RAIL, TimelineStyle.ADAPTIVE_RAIL, TimelineStyle.STAR_RAIL -> {
                val railCount = (segmentCount - 2).coerceAtLeast(1)
                val filledSegments = (progressFraction * railCount).toInt().coerceIn(0, railCount)
                val segmentWidth = size.width / segmentCount
                val y = size.height / 2f - (paint.descent() + paint.ascent()) / 2f

                // Opening bracket '['
                paint.color = primaryColor.toArgb()
                drawContext.canvas.nativeCanvas.drawText("[", segmentWidth / 2f, y, paint)

                // Rail segments
                for (i in 0 until railCount) {
                    val isTip = (i == filledSegments - 1 && filledSegments > 0) || (i == 0 && filledSegments == 0 && progressFraction > 0f)
                    val isFilled = (i < filledSegments - 1)

                    val char = if (style == TimelineStyle.ARROW_RAIL) {
                        when {
                            isTip -> ">"
                            isFilled || i < filledSegments -> "="
                            else -> "."
                        }
                    } else {
                        // ADAPTIVE_RAIL / STAR_RAIL: - -> * -> o
                        when {
                            isTip -> "*"
                            isFilled -> "o"
                            else -> "-"
                        }
                    }

                    paint.color = when {
                        isTip -> tipColor.toArgb()
                        isFilled -> primaryColor.toArgb()
                        else -> trackColor.toArgb()
                    }

                    val x = (i + 1) * segmentWidth + (segmentWidth / 2f)
                    drawContext.canvas.nativeCanvas.drawText(char, x, y, paint)
                }

                // Closing bracket ']'
                paint.color = primaryColor.toArgb()
                val lastX = (segmentCount - 1) * segmentWidth + (segmentWidth / 2f)
                drawContext.canvas.nativeCanvas.drawText("]", lastX, y, paint)
            }
        }
    }
}
