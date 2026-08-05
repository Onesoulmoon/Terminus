package com.necroware.terminusplayer.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class AreaPoint(val label: String, val value: Float)

/**
 * Real Canvas-drawn filled area chart — each point is one month for the
 * yearly listening-habits view. Dashed grid behind, filled area under the
 * line at low alpha, solid line on top, matching the terminal palette.
 */
@Composable
fun AreaChart(
    points: List<AreaPoint>,
    modifier: Modifier = Modifier,
    height: Dp = 140.dp
) {
    val lineColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outline

    Column(modifier = modifier.fillMaxWidth()) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
        ) {
            val maxValue = (points.maxOfOrNull { it.value } ?: 0f).coerceAtLeast(1f)
            val stepX = if (points.size > 1) size.width / (points.size - 1) else size.width

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

            if (points.size < 2) return@Canvas

            val coords = points.mapIndexed { index, point ->
                Offset(index * stepX, size.height - (size.height * (point.value / maxValue)))
            }

            val fillPath = Path().apply {
                moveTo(coords.first().x, size.height)
                coords.forEach { lineTo(it.x, it.y) }
                lineTo(coords.last().x, size.height)
                close()
            }
            drawPath(fillPath, color = lineColor.copy(alpha = 0.22f))

            val linePath = Path().apply {
                moveTo(coords.first().x, coords.first().y)
                coords.drop(1).forEach { lineTo(it.x, it.y) }
            }
            drawPath(linePath, color = lineColor, style = Stroke(width = 3f))
        }

        Row(modifier = Modifier.fillMaxWidth().padding(top = 6.dp)) {
            points.forEach { point ->
                Text(
                    text = point.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
