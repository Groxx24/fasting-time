package com.fasting.time.ui.fasting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fasting.time.domain.model.FastingPhase
import com.fasting.time.domain.usecase.ObserveFastingTimerUseCase
import com.fasting.time.domain.usecase.StartPhaseUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration

data class FastingUiState(
    /** True until the saved phase has been read, so the first frame doesn't guess. */
    val isLoading: Boolean = true,
    /** The phase under way, or null before the first one is started. */
    val phase: FastingPhase? = null,
    val elapsed: Duration = Duration.ZERO,
)

class FastingViewModel(
    observeFastingTimer: ObserveFastingTimerUseCase,
    private val startPhase: StartPhaseUseCase,
) : ViewModel() {

    /** Ticks only while it is collected, and picks the time up again when collection resumes. */
    val uiState: StateFlow<FastingUiState> =
        observeFastingTimer()
            .map { timer ->
                FastingUiState(
                    isLoading = false,
                    phase = timer?.phase,
                    elapsed = timer?.elapsed ?: Duration.ZERO,
                )
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), FastingUiState())

    fun start(phase: FastingPhase) {
        viewModelScope.launch { startPhase(phase) }
    }
}
