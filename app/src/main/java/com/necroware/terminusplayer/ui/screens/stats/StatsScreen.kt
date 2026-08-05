package com.necroware.terminusplayer.ui.screens.stats

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.necroware.terminusplayer.data.repository.StatsRange
import com.necroware.terminusplayer.data.repository.TopCategory
import com.necroware.terminusplayer.data.repository.TopCategoryItem
import com.necroware.terminusplayer.ui.components.BarDatum
import com.necroware.terminusplayer.ui.components.BlockyBarChart
import com.necroware.terminusplayer.ui.components.TerminalBorder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun StatsScreen(viewModel: StatsViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Text(
                text = "> STATS_",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 20.dp)
            )
        }

        if (state.insights.isNotEmpty()) {
            items(state.insights) { insight ->
                TerminalBorder(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = insight,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatsRange.entries.forEach { range ->
                    Text(
                        text = "[${range.label}]",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (range == state.range) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.clickable { viewModel.selectRange(range) }
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryTile(
                    label = "TOTAL PLAYS",
                    value = state.totalPlays.toString(),
                    modifier = Modifier.weight(1f)
                )
                SummaryTile(
                    label = "LISTENING TIME",
                    value = formatDuration(state.totalMsPlayed),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryTile(
                    label = "AVG SESSION",
                    value = if (state.sessionStats.totalSessions > 0) formatDuration(state.sessionStats.avgSessionMs) else "—",
                    modifier = Modifier.weight(1f)
                )
                SummaryTile(
                    label = "SESSIONS",
                    value = state.sessionStats.totalSessions.toString(),
                    modifier = Modifier.weight(1f)
                )
                SummaryTile(
                    label = "LONGEST",
                    value = if (state.sessionStats.totalSessions > 0) formatDuration(state.sessionStats.longestSessionMs) else "—",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Column {
                Text(
                    text = when (state.range) {
                        StatsRange.TODAY -> "LISTENING TIMELINE (HOUR)"
                        StatsRange.MONTH -> "ARTIST DISTRIBUTION (PIE)"
                        StatsRange.YEAR -> "MONTHLY LISTENING (AREA)"
                        StatsRange.ALL_TIME -> "ALL-TIME DISTRIBUTION (SCATTER)"
                        StatsRange.WEEK -> "PLAYS BY DAY (BAR)"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                when (state.range) {
                    StatsRange.TODAY -> {
                        if (state.playsByHour.isNotEmpty()) {
                            val countByHour = state.playsByHour.associate { it.hourOfDay to it.playCount }
                            val fullDayValues = (0..23).map { hour -> (countByHour[hour] ?: 0).toFloat() }
                            com.necroware.terminusplayer.ui.components.LineChart(values = fullDayValues)
                        } else if (!state.isLoading) {
                            EmptyChartMessage()
                        }
                    }
                    StatsRange.MONTH -> {
                        if (state.monthlyArtistDistribution.isNotEmpty()) {
                            com.necroware.terminusplayer.ui.components.PieChart(
                                slices = state.monthlyArtistDistribution.map {
                                    com.necroware.terminusplayer.ui.components.PieSlice(it.first, it.second.toFloat())
                                }
                            )
                        } else if (!state.isLoading) {
                            EmptyChartMessage()
                        }
                    }
                    StatsRange.YEAR -> {
                        if (state.yearlyMonthlyTotals.isNotEmpty()) {
                            com.necroware.terminusplayer.ui.components.AreaChart(
                                points = state.yearlyMonthlyTotals.map {
                                    com.necroware.terminusplayer.ui.components.AreaPoint(monthLabelShort(it.first), it.second.toFloat())
                                }
                            )
                        } else if (!state.isLoading) {
                            EmptyChartMessage()
                        }
                    }
                    StatsRange.ALL_TIME -> {
                        if (state.playsByDay.isNotEmpty()) {
                            com.necroware.terminusplayer.ui.components.ScatterPlot(
                                values = state.playsByDay.map { it.playCount.toFloat() }
                            )
                        } else if (!state.isLoading) {
                            EmptyChartMessage()
                        }
                    }
                    StatsRange.WEEK -> {
                        if (state.playsByDay.isNotEmpty()) {
                            val barData = state.playsByDay.map { day ->
                                BarDatum(
                                    label = dayFormat().format(Date(day.dayEpoch)),
                                    value = day.playCount.toFloat()
                                )
                            }
                            BlockyBarChart(data = barData)
                        } else if (!state.isLoading) {
                            EmptyChartMessage()
                        }
                    }
                }
            }
        }

        item {
            Column {
                Text(
                    text = "TOP CATEGORIES",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TopCategory.entries.forEach { category ->
                        Text(
                            text = "[${category.label}]",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (category == state.category) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.clickable { viewModel.selectCategory(category) }
                        )
                    }
                }
            }
        }

        if (state.topCategoryItems.isEmpty() && !state.isLoading) {
            item {
                Text(
                    text = "[ nothing here yet for this range ]",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(state.topCategoryItems, key = { it.label }) { item ->
            TopCategoryRow(item)
        }
    }
}

@Composable
private fun SummaryTile(label: String, value: String, modifier: Modifier = Modifier) {
    TerminalBorder(modifier = modifier.fillMaxWidth()) {
        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TopCategoryRow(item: TopCategoryItem) {
    TerminalBorder(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = item.label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${item.playCount} plays",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun dayFormat(): SimpleDateFormat = SimpleDateFormat("EEE", Locale.getDefault())

@Composable
private fun EmptyChartMessage() {
    Text(
        text = "[ no plays recorded in this range yet ]",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/** "2026-01" -> "Jan" */
private fun monthLabelShort(monthLabel: String): String {
    val parts = monthLabel.split("-")
    val monthIndex = parts.getOrNull(1)?.toIntOrNull() ?: return monthLabel
    val names = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    return names.getOrElse(monthIndex - 1) { monthLabel }
}

private fun formatDuration(ms: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(ms)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(ms) % 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}
