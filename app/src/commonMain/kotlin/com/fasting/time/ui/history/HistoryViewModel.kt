package com.fasting.time.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fasting.time.domain.model.Day
import com.fasting.time.domain.model.DaySummary
import com.fasting.time.domain.model.FastingPhase
import com.fasting.time.domain.model.HistoryGrouping
import com.fasting.time.domain.model.PeriodSummary
import com.fasting.time.domain.usecase.ObserveFastingTimerUseCase
import com.fasting.time.domain.usecase.ObserveHistoryUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

data class HistoryUiState(
    val isLoading: Boolean = true,
    val grouping: HistoryGrouping = HistoryGrouping.Week,
    val today: Day = Day(0),
    /** The longest fast ever finished, or null before the first one. */
    val longestFast: Duration? = null,
    /** How long the fast under way has lasted, or null while not fasting. */
    val currentFast: Duration? = null,
    /** Newest first. */
    val periods: List<PeriodSummary> = emptyList(),
) {
    companion object {
        val Preview = HistoryUiState(
            isLoading = false,
            today = Day(20_728),
            longestFast = 17.hours + 20.minutes,
            currentFast = 9.hours + 5.minutes,
            periods = listOf(
                PeriodSummary(
                    start = Day(20_724),
                    days = listOf(
                        DaySummary(Day(20_727), 16.hours + 10.minutes, 7.hours + 45.minutes),
                        DaySummary(Day(20_726), 17.hours + 20.minutes, 6.hours + 50.minutes),
                        DaySummary(Day(20_725), 12.hours + 30.minutes, 8.hours),
                    ),
                ),
            ),
        )
    }
}

class HistoryViewModel(
    private val observeHistory: ObserveHistoryUseCase,
    observeFastingTimer: ObserveFastingTimerUseCase,
) : ViewModel() {

    private val grouping = MutableStateFlow(HistoryGrouping.Week)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HistoryUiState> =
        combine(
            grouping.flatMapLatest { grouping -> observeHistory(grouping).map { grouping to it } },
            observeFastingTimer(),
        ) { (grouping, history), timer ->
            HistoryUiState(
                isLoading = false,
                grouping = grouping,
                today = history.today,
                longestFast = history.longestFast,
                currentFast = timer?.takeIf { it.phase == FastingPhase.Fasting }?.elapsed,
                periods = history.periods,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), HistoryUiState())

    fun setGrouping(grouping: HistoryGrouping) {
        this.grouping.value = grouping
    }
}
