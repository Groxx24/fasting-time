package com.fasting.time.ui.fasting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fasting.time.domain.model.FastingTimer
import com.fasting.time.domain.usecase.ObserveFastingTimerUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class FastingUiState(
    /** Null until the first reading, so the first frame doesn't guess. */
    val timer: FastingTimer? = null,
)

class FastingViewModel(observeFastingTimer: ObserveFastingTimerUseCase) : ViewModel() {

    /** Ticks only while it is collected, and picks the time up again when collection resumes. */
    val uiState: StateFlow<FastingUiState> =
        observeFastingTimer()
            .map { timer -> FastingUiState(timer) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), FastingUiState())
}
