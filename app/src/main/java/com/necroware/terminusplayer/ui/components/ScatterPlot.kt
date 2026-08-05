package com.necroware.terminusplayer.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Real Canvas-drawn scatter plot — unconnected points, dashed grid behind.
 * Used for the All-Time distribution view, where a continuous line would
 * imply a trend that a scatter more honestly represents as discrete days.
 */
@Composable
fun ScatterPlot(
    values: List<Float>,
    modifier: Modifier = Modifier,
    height: Dp = 140.dp
) {
    val dotColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outline

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val maxValue = (values.maxOrNull() ?: 0f).coerceAtLeast(1f)
        val stepX = if (values.size > 1) size.width / (values.size - 1) else size.width

        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
        for (i in 0..4) {
            val y = size.height * (i / 4f)
            drawLine(
                color = gridColor,
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f,
                pathEffect = dashEffect
            )
        }

        values.forEachIndexed { index, value ->
            val x = index * stepX
            val y = size.height - (size.height * (value / maxValue))
            drawCircle(color = dotColor, radius = 3.5f, center = Offset(x, y))
        }
    }
}
