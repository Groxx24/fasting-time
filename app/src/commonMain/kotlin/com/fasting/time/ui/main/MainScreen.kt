package com.fasting.time.ui.main

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fasting.time.di.AppContainer
import com.fasting.time.domain.model.FastingPhase
import com.fasting.time.domain.model.FastingTimer
import com.fasting.time.domain.model.FastingWindow
import com.fasting.time.ui.fasting.FastingBackdrop
import com.fasting.time.ui.fasting.FastingRoute
import com.fasting.time.ui.fasting.FastingScreen
import com.fasting.time.ui.fasting.FastingUiState
import com.fasting.time.ui.setup.SetupScreen
import com.fasting.time.ui.theme.FastingTimeTheme
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

@Composable
fun MainRoute(container: AppContainer) {
    val viewModel = viewModel {
        MainViewModel(container.observeFastingTimer, container.setFastingWindow)
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var isChangingWindow by rememberSaveable { mutableStateOf(false) }

    MainScreen(
        state = state,
        isChangingWindow = isChangingWindow,
        onSetWindow = { window ->
            viewModel.setWindow(window)
            isChangingWindow = false
        },
    ) {
        FastingRoute(container, onChangeWindow = { isChangingWindow = true })
    }
}

/**
 * The frame both screens live in: the backdrop behind, and on it the setup until a window is
 * chosen or while it is being changed, otherwise the timer. The backdrop is shared so that it
 * keeps moving between the two. It must fill the window, because the backdrop is told where the
 * timer is in window coordinates.
 */
@Composable
fun MainScreen(
    state: MainUiState,
    isChangingWindow: Boolean,
    onSetWindow: (FastingWindow) -> Unit,
    modifier: Modifier = Modifier,
    timer: @Composable () -> Unit,
) {
    var contentBounds by remember { mutableStateOf<Rect?>(null) }
    val showSetup = state.window == null || isChangingWindow

    Box(modifier.fillMaxSize()) {
        FastingBackdrop(
            phase = state.phase,
            showSign = !showSetup,
            signBounds = { contentBounds },
        )
        // Nothing until the saved window has been read, so the first frame doesn't guess.
        if (!state.isLoading) {
            Crossfade(
                targetState = showSetup,
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .onGloballyPositioned { contentBounds = it.boundsInRoot() },
                label = "screen",
            ) { setup ->
                if (setup) {
                    SetupScreen(
                        initial = state.window ?: FastingWindow.Default,
                        onConfirm = onSetWindow,
                    )
                } else {
                    timer()
                }
            }
        }
    }
}

@Preview
@Composable
private fun MainScreenTimerPreview() {
    val window = FastingWindow.Default
    FastingTimeTheme {
        MainScreen(
            state = MainUiState(isLoading = false, window, FastingPhase.Fasting),
            isChangingWindow = false,
            onSetWindow = {},
        ) {
            FastingScreen(
                state = FastingUiState(
                    FastingTimer(FastingPhase.Fasting, 14.hours + 7.minutes, window),
                ),
                onChangeWindow = {},
            )
        }
    }
}

@Preview
@Composable
private fun MainScreenSetupPreview() {
    FastingTimeTheme {
        MainScreen(state = MainUiState(isLoading = false), isChangingWindow = false, onSetWindow = {}) {}
    }
}
