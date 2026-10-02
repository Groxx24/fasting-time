package com.fasting.time.ui.main

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fasting.time.di.AppContainer
import com.fasting.time.domain.model.FastingPhase
import com.fasting.time.resources.Res
import com.fasting.time.resources.ic_history
import com.fasting.time.resources.ic_timer
import com.fasting.time.resources.tab_history
import com.fasting.time.resources.tab_timer
import com.fasting.time.ui.components.PillSelector
import com.fasting.time.ui.fasting.FastingBackdrop
import com.fasting.time.ui.fasting.FastingRoute
import com.fasting.time.ui.fasting.FastingScreen
import com.fasting.time.ui.fasting.FastingUiState
import com.fasting.time.ui.history.HistoryRoute
import com.fasting.time.ui.history.HistoryScreen
import com.fasting.time.ui.history.HistoryUiState
import com.fasting.time.ui.theme.FastingTimeTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

enum class MainTab(val icon: DrawableResource, val label: StringResource) {
    Timer(Res.drawable.ic_timer, Res.string.tab_timer),
    History(Res.drawable.ic_history, Res.string.tab_history),
}

@Composable
fun MainRoute(container: AppContainer) {
    val viewModel = viewModel { MainViewModel(container.observeFastingTimer) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.Timer) }

    MainScreen(
        state = state,
        selectedTab = selectedTab,
        onSelectTab = { selectedTab = it },
    ) { tab ->
        when (tab) {
            MainTab.Timer -> FastingRoute(container)
            MainTab.History -> HistoryRoute(container)
        }
    }
}

/**
 * The frame every tab lives in: the backdrop behind, the tab bar below. The backdrop is shared so
 * that it keeps moving while switching tabs. It must fill the window, because the backdrop is told
 * where the tabs are in window coordinates.
 */
@Composable
fun MainScreen(
    state: MainUiState,
    selectedTab: MainTab,
    onSelectTab: (MainTab) -> Unit,
    modifier: Modifier = Modifier,
    tabContent: @Composable (MainTab) -> Unit,
) {
    var tabBounds by remember { mutableStateOf<Rect?>(null) }

    Box(modifier.fillMaxSize()) {
        FastingBackdrop(
            phase = state.phase,
            showSign = selectedTab == MainTab.Timer,
            signBounds = { tabBounds },
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Crossfade(
                targetState = selectedTab,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .onGloballyPositioned { tabBounds = it.boundsInRoot() },
                label = "tab",
            ) { tab ->
                tabContent(tab)
            }
            PillSelector(
                options = MainTab.entries,
                selected = selectedTab,
                onSelect = onSelectTab,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
            ) { tab ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(painter = painterResource(tab.icon), contentDescription = null)
                    Text(
                        text = stringResource(tab.label),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun MainScreenTimerPreview() {
    FastingTimeTheme {
        MainScreen(
            state = MainUiState(FastingPhase.Fasting),
            selectedTab = MainTab.Timer,
            onSelectTab = {},
        ) {
            FastingScreen(
                state = FastingUiState(
                    isLoading = false,
                    phase = FastingPhase.Fasting,
                    elapsed = 14.hours + 7.minutes,
                ),
                onStart = {},
            )
        }
    }
}

@Preview
@Composable
private fun MainScreenHistoryPreview() {
    FastingTimeTheme {
        MainScreen(
            state = MainUiState(FastingPhase.Eating),
            selectedTab = MainTab.History,
            onSelectTab = {},
        ) {
            HistoryScreen(state = HistoryUiState.Preview, onGroupingChange = {})
        }
    }
}
