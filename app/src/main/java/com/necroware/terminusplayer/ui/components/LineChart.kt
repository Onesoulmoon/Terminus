package com.necroware.terminusplayer.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Real Canvas-drawn line chart with a dashed grid — per the original spec
 * that line/area/pie/scatter/box chart types render as actual geometry
 * with terminal-palette dashed/grid styling, not a character-grid ASCII
 * approximation (which gets unreadable fast for this kind of data).
 */
@Composable
fun LineChart(
    values: List<Float>,
    modifier: Modifier = Modifier,
    height: Dp = 140.dp,
    gridLines: Int = 4
) {
    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outline

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val maxValue = (values.maxOrNull() ?: 0f).coerceAtLeast(1f)
        val stepX = if (values.size > 1) size.width / (values.size - 1) else size.width

        // Dashed horizontal grid lines.
        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
        for (i in 0..gridLines) {
            val y = size.height * (i.toFloat() / gridLines)
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f,
                pathEffect = dashEffect
            )
        }

        if (values.size < 2) return@Canvas

        val points = values.mapIndexed { index, value ->
            Offset(
                x = index * stepX,
                y = size.height - (size.height * (value / maxValue))
            )
        }

        for (i in 0 until points.size - 1) {
            drawLine(
                color = lineColor,
                start = points[i],
                end = points[i + 1],
                strokeWidth = 3f,
                cap = StrokeCap.Round
            )
        }

        points.forEach { point ->
            drawCircle(color = lineColor, radius = 4f, center = point)
        }
    }
}
