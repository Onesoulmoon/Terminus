package com.necroware.terminusplayer.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.necroware.terminusplayer.data.prefs.TimelineStyle
import com.necroware.terminusplayer.ui.theme.LocalTerminalPalette
import com.necroware.terminusplayer.ui.theme.TerminalFontFamily

/**
 * Customizable ASCII timeline seekbar with 3 selectable styles:
 * - HIGHLIGHTED_BLOCKS: [█████░░░░] with highlighted active playhead tip
 * - ARROW_RAIL: [=======>........]
 * - ADAPTIVE_RAIL: [ooooo*-------] progressive morphing rail with shifting head (x -> o -> O -> *)
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

    // Morphing character sequence for active progress head
    val morphFrames = listOf("x", "o", "O", "*", "o", "x")
    val infiniteTransition = rememberInfiniteTransition(label = "morphRail")
    val frameIndex by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = morphFrames.size.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "morphIndex"
    )
    val currentMorphChar = morphFrames[frameIndex.toInt().coerceIn(0, morphFrames.size - 1)]

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
                        // ADAPTIVE_RAIL / STAR_RAIL: - -> morphing head -> o
                        when {
                            isTip -> currentMorphChar
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

/**
 * Morphing Character-Shift Progress Rail (Standalone text-based rail composable)
 * Tip character dynamically transforms through shifting sequence: x -> o -> O -> * -> o -> x
 */
@Composable
fun MorphingAsciiTimelineRail(
    progress: Float, // 0.0f to 1.0f
    onSeek: (Float) -> Unit,
    modifier: Modifier = Modifier,
    totalBars: Int = 24,
    accentColor: Color = LocalTerminalPalette.current.primaryAccent
) {
    val morphFrames = listOf("x", "o", "O", "*", "o", "x")

    val infiniteTransition = rememberInfiniteTransition(label = "morphRail")
    val frameIndex by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = morphFrames.size.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "morphIndex"
    )

    val currentMorphChar = morphFrames[frameIndex.toInt().coerceIn(0, morphFrames.size - 1)]
    val currentPosition = (progress * totalBars).toInt().coerceIn(0, totalBars)

    val railString = remember(progress, currentPosition, currentMorphChar) {
        buildString {
            append("[")
            for (i in 0 until totalBars) {
                when {
                    i < currentPosition - 1 -> append("-")
                    i == currentPosition - 1 -> append(currentMorphChar)
                    else -> append(" ")
                }
            }
            append("]")
        }
    }

    Row(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    val newProgress = (offset.x / size.width).coerceIn(0f, 1f)
                    onSeek(newProgress)
                }
            }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = railString,
            color = accentColor,
            fontFamily = TerminalFontFamily,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
