package com.necroware.terminusplayer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class BarDatum(val label: String, val value: Float)

/**
 * Real blocky bars — a Row of solid-colored Boxes whose heights are
 * proportional to value, NOT a Canvas drawing. Matches the terminal
 * "block chart" look for bar/histogram chart types specifically.
 */
@Composable
fun BlockyBarChart(
    data: List<BarDatum>,
    modifier: Modifier = Modifier,
    chartHeight: Dp = 120.dp,
    barColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary
) {
    val maxValue = data.maxOfOrNull { it.value }?.takeIf { it > 0f } ?: 1f

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        data.forEach { datum ->
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .height(chartHeight),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    val barHeight = chartHeight * (datum.value / maxValue).coerceIn(0.02f, 1f)
                    Box(
                        modifier = Modifier
                            .width(20.dp)
                            .height(barHeight)
                            .background(barColor)
                    )
                }
                Text(
                    text = datum.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
