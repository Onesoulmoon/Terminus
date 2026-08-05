package com.necroware.terminusplayer.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.necroware.terminusplayer.data.database.dao.DayPlayCount
import com.necroware.terminusplayer.data.database.dao.HourHistogramRow
import com.necroware.terminusplayer.data.repository.SessionStats
import com.necroware.terminusplayer.data.repository.StatsRange
import com.necroware.terminusplayer.data.repository.StatsRepository
import com.necroware.terminusplayer.data.repository.TopCategory
import com.necroware.terminusplayer.data.repository.TopCategoryItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StatsUiState(
    val range: StatsRange = StatsRange.WEEK,
    val isLoading: Boolean = true,
    val totalPlays: Int = 0,
    val totalMsPlayed: Long = 0L,
    val playsByDay: List<DayPlayCount> = emptyList(),
    val playsByHour: List<HourHistogramRow> = emptyList(),
    val sessionStats: SessionStats = SessionStats(0, 0L, 0L),
    val category: TopCategory = TopCategory.ARTIST,
    val topCategoryItems: List<TopCategoryItem> = emptyList(),
    val insights: List<String> = emptyList(),
    // Range-specific chart data — each StatsRange gets its own chart type.
    val monthlyArtistDistribution: List<Pair<String, Int>> = emptyList(),
    val yearlyMonthlyTotals: List<Pair<String, Int>> = emptyList()
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val statsRepository: StatsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        loadInsightsOnce()
        load(StatsRange.WEEK, TopCategory.ARTIST)
    }

    fun selectRange(range: StatsRange) {
        load(range, _uiState.value.category)
    }

    fun selectCategory(category: TopCategory) {
        load(_uiState.value.range, category)
    }

    private fun load(range: StatsRange, category: TopCategory) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(range = range, category = category, isLoading = true)
            val summary = statsRepository.getSummary(range)
            val topItems = statsRepository.getTopCategory(range, category)
            val sessionStats = statsRepository.getSessionStats(range)

            // Only fetch the chart data the currently-selected range actually needs.
            val monthlyArtists = if (range == StatsRange.MONTH) statsRepository.getMonthlyArtistDistribution() else emptyList()
            val yearlyMonthly = if (range == StatsRange.YEAR) statsRepository.getYearlyMonthlyTotals() else emptyList()

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                totalPlays = summary.totalPlays,
                totalMsPlayed = summary.totalMsPlayed,
                playsByDay = summary.playsByDay,
                playsByHour = summary.playsByHour,
                sessionStats = sessionStats,
                topCategoryItems = topItems,
                monthlyArtistDistribution = monthlyArtists,
                yearlyMonthlyTotals = yearlyMonthly
            )
        }
    }

    private fun loadInsightsOnce() {
        viewModelScope.launch {
            val insights = statsRepository.generateInsights()
            _uiState.value = _uiState.value.copy(insights = insights)
        }
    }
}
