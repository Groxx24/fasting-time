package com.fasting.time.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fasting.time.domain.model.FastingPhase
import com.fasting.time.domain.usecase.ObserveFastingTimerUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class MainUiState(
    /** The phase under way, which the backdrop behind every tab shows. Null before the first. */
    val phase: FastingPhase? = null,
)

class MainViewModel(observeFastingTimer: ObserveFastingTimerUseCase) : ViewModel() {
    val uiState: StateFlow<MainUiState> =
        observeFastingTimer()
            .map { timer -> MainUiState(timer?.phase) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), MainUiState())
}
