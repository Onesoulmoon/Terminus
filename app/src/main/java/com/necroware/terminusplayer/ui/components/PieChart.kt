package com.necroware.terminusplayer.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class PieSlice(val label: String, val value: Float)

/**
 * Real Canvas-drawn pie chart. A single-hue terminal palette doesn't give
 * us distinct colors per slice, so slices are shaded via alternating alpha
 * levels of the accent color instead, with a text legend beside it.
 */
@Composable
fun PieChart(
    slices: List<PieSlice>,
    modifier: Modifier = Modifier,
    diameter: Dp = 160.dp
) {
    val accent = MaterialTheme.colorScheme.primary
    val total = slices.sumOf { it.value.toDouble() }.toFloat().coerceAtLeast(0.0001f)
    val alphas = listOf(1f, 0.75f, 0.55f, 0.4f, 0.28f, 0.18f)

    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(diameter)) {
            Canvas(modifier = Modifier.size(diameter)) {
                var startAngle = -90f
                slices.forEachIndexed { index, slice ->
                    val sweep = (slice.value / total) * 360f
                    drawArc(
                        color = accent.copy(alpha = alphas.getOrElse(index) { 0.15f }),
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = true,
                        size = Size(size.width, size.height)
                    )
                    startAngle += sweep
                }
            }
        }
        Column(
            modifier = Modifier.padding(start = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            slices.forEachIndexed { index, slice ->
                val percent = ((slice.value / total) * 100).toInt()
                Row {
                    Canvas(modifier = Modifier.size(10.dp)) {
                        drawRect(color = accent.copy(alpha = alphas.getOrElse(index) { 0.15f }))
                    }
                    Text(
                        text = "  ${slice.label} · $percent%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
