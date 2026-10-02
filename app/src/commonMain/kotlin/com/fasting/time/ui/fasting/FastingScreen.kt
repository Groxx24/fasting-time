package com.fasting.time.ui.fasting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fasting.time.di.AppContainer
import com.fasting.time.domain.model.FastingPhase
import com.fasting.time.domain.model.FastingTimer
import com.fasting.time.domain.model.FastingWindow
import com.fasting.time.resources.Res
import com.fasting.time.resources.caption_eating
import com.fasting.time.resources.caption_fasting
import com.fasting.time.resources.change_window
import com.fasting.time.resources.phase_eating
import com.fasting.time.resources.phase_fasting
import com.fasting.time.ui.components.panel
import com.fasting.time.ui.format.toClockText
import com.fasting.time.ui.format.toTimerText
import org.jetbrains.compose.resources.stringResource

@Composable
fun FastingRoute(
    container: AppContainer,
    onChangeWindow: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel = viewModel { FastingViewModel(container.observeFastingTimer) }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    FastingScreen(state = state, onChangeWindow = onChangeWindow, modifier = modifier)
}

/** The timer in the middle of the screen, over the sign, and the window it follows below. */
@Composable
fun FastingScreen(
    state: FastingUiState,
    onChangeWindow: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val timer = state.timer ?: return
    Box(modifier = modifier.fillMaxSize().padding(24.dp)) {
        // The timer stays in the middle of the screen, over the sign, so it keeps clear
        // of the window by leaving its height free both below and above itself.
        var windowHeight by remember { mutableIntStateOf(0) }
        val clearance = with(LocalDensity.current) { windowHeight.toDp() } + 16.dp
        Readout(
            timer = timer,
            modifier = Modifier.align(Alignment.Center).padding(vertical = clearance),
        )
        WindowButton(
            window = timer.window,
            onClick = onChangeWindow,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .onSizeChanged { windowHeight = it.height },
        )
    }
}

@Composable
private fun Readout(timer: FastingTimer, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(
                when (timer.phase) {
                    FastingPhase.Fasting -> Res.string.phase_fasting
                    FastingPhase.Eating -> Res.string.phase_eating
                },
            ).uppercase(),
            style = ReadoutStyle.copy(fontSize = 20.sp, letterSpacing = 4.sp),
        )
        // As big as the space allows, so the same text fills a phone held either way.
        BasicText(
            text = timer.remaining.toTimerText(),
            // Measured after the lines around it, so it takes only the height they leave.
            modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
            style = ReadoutStyle.copy(
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                // Same-width digits, so the time doesn't shuffle sideways as it ticks.
                fontFeatureSettings = "tnum",
            ),
            maxLines = 1,
            softWrap = false,
            autoSize = TextAutoSize.StepBased(minFontSize = 32.sp, maxFontSize = 128.sp),
        )
        // What the time above counts down to.
        Text(
            text = when (timer.phase) {
                FastingPhase.Fasting ->
                    stringResource(Res.string.caption_fasting, timer.window.end.toClockText())
                FastingPhase.Eating ->
                    stringResource(Res.string.caption_eating, timer.window.start.toClockText())
            },
            style = ReadoutStyle.copy(fontSize = 18.sp),
        )
    }
}

/** The window the timer follows. Tapping it is the way back to choosing another. */
@Composable
private fun WindowButton(window: FastingWindow, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(
            Res.string.change_window,
            window.start.toClockText(),
            window.end.toClockText(),
        ),
        modifier = modifier
            .panel(CircleShape)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        style = MaterialTheme.typography.titleMedium,
    )
}

private val ReadoutStyle = TextStyle(
    color = Color.White,
    fontWeight = FontWeight.Medium,
    // Keeps white text readable when a utensil or the bar of the sign passes behind it.
    shadow = Shadow(color = Color.Black.copy(alpha = 0.35f), blurRadius = 24f),
)
