package com.fasting.time.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fasting.time.domain.model.FastingPhase
import com.fasting.time.domain.model.FastingWindow
import com.fasting.time.domain.usecase.ObserveFastingTimerUseCase
import com.fasting.time.domain.usecase.SetFastingWindowUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class MainUiState(
    /** True until the saved window has been read, so the first frame doesn't guess. */
    val isLoading: Boolean = true,
    /** The window to fast in, or null until the user chooses one. */
    val window: FastingWindow? = null,
    /** The phase it is by that window, which the backdrop shows. */
    val phase: FastingPhase? = null,
)

class MainViewModel(
    observeFastingTimer: ObserveFastingTimerUseCase,
    private val setFastingWindow: SetFastingWindowUseCase,
) : ViewModel() {
    val uiState: StateFlow<MainUiState> =
        observeFastingTimer()
            .map { timer -> MainUiState(isLoading = false, timer?.window, timer?.phase) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(), MainUiState())

    fun setWindow(window: FastingWindow) {
        viewModelScope.launch { setFastingWindow(window) }
    }
}
