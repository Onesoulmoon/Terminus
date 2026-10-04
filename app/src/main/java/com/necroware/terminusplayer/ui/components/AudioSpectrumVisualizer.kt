package com.necroware.terminusplayer.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.necroware.terminusplayer.data.prefs.VisualizerMode
import kotlin.math.sin

@Composable
fun AudioSpectrumVisualizer(
    fftData: FloatArray, // normalized magnitude float values (0.0f to 1.0f)
    mode: VisualizerMode,
    modifier: Modifier = Modifier,
    accentColor: Color = Color.White
) {
    val infiniteTransition = rememberInfiniteTransition(label = "phase")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phaseAnim"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .background(Color.Black.copy(alpha = 0.25f)) // Low transparency overlay fill
            .border(1.dp, accentColor.copy(alpha = 0.35f))
            .clip(RectangleShape)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val centerH = height / 2f
            val count = fftData.size.coerceAtLeast(1)

            when (mode) {
                VisualizerMode.WAVE_MESH -> {
                    val waveLayers = 6
                    val stepX = if (count > 1) width / (count - 1).toFloat() else width

                    for (layer in 0 until waveLayers) {
                        val path = Path()
                        val layerAlpha = 1.0f - (layer * 0.15f)
                        val layerPhaseOffset = phase + (layer * 0.4f)
                        val layerScale = 1.0f - (layer * 0.12f)

                        for (i in 0 until count) {
                            val x = i * stepX
                            val mag = (fftData.getOrNull(i) ?: 0.1f) * layerScale
                            val waveOffset = sin(i * 0.3f + layerPhaseOffset) * (mag * height * 0.22f)
                            val y = (centerH - (mag * height * 0.3f) + waveOffset).coerceIn(4f, height - 4f)

                            if (i == 0) {
                                path.moveTo(x, y)
                            } else {
                                val prevX = (i - 1) * stepX
                                val prevMag = (fftData.getOrNull(i - 1) ?: 0.1f) * layerScale
                                val prevWave = sin((i - 1) * 0.3f + layerPhaseOffset) * (prevMag * height * 0.22f)
                                val prevY = (centerH - (prevMag * height * 0.3f) + prevWave).coerceIn(4f, height - 4f)

                                val cx = (prevX + x) / 2f
                                path.cubicTo(cx, prevY, cx, y, x, y)
                            }
                        }

                        drawPath(
                            path = path,
                            color = accentColor.copy(alpha = layerAlpha.coerceIn(0.1f, 1f)),
                            style = Stroke(width = if (layer == 0) 2.5f else 1.2f)
                        )
                    }
                }

                VisualizerMode.DOT_MATRIX -> {
                    val cols = count
                    val rows = 14
                    val colWidth = width / cols
                    val rowHeight = height / rows
                    val dotRadius = (colWidth.coerceAtMost(rowHeight) * 0.35f)

                    for (c in 0 until cols) {
                        val mag = fftData.getOrNull(c) ?: 0.05f
                        val litRows = (mag * rows).toInt()

                        val cx = (c * colWidth) + (colWidth / 2f)

                        for (r in 0 until rows) {
                            val cy = height - ((r * rowHeight) + (rowHeight / 2f))
                            val isLit = r <= litRows

                            val dotColor = when {
                                isLit && r > rows * 0.85f -> Color(0xFFFF0055)
                                isLit -> accentColor
                                else -> accentColor.copy(alpha = 0.08f)
                            }

                            drawCircle(
                                color = dotColor,
                                radius = dotRadius,
                                center = Offset(cx, cy)
                            )
                        }
                    }
                }

                VisualizerMode.BARS -> {
                    val colWidth = width / count.toFloat()

                    for (i in 0 until count) {
                        val mag = (fftData.getOrNull(i) ?: 0.08f).coerceAtLeast(0.06f)
                        val barHeight = (mag * height).coerceAtLeast(3.dp.toPx())
                        val left = i * colWidth
                        val top = height - barHeight

                        drawRect(
                            color = accentColor,
                            topLeft = Offset(left, top),
                            size = Size((colWidth - 1f).coerceAtLeast(1f), barHeight)
                        )
                    }
                }

                VisualizerMode.SYMMETRIC_BARS -> {
                    val colWidth = width / count.toFloat()

                    for (i in 0 until count) {
                        val mag = (fftData.getOrNull(i) ?: 0.08f).coerceAtLeast(0.06f)
                        val barHeight = (mag * height * 0.85f).coerceAtLeast(3.dp.toPx())
                        val left = i * colWidth
                        val top = centerH - (barHeight / 2f)

                        drawRect(
                            color = accentColor,
                            topLeft = Offset(left, top),
                            size = Size((colWidth - 1f).coerceAtLeast(1f), barHeight)
                        )
                    }
                }
            }
        }
    }
}
