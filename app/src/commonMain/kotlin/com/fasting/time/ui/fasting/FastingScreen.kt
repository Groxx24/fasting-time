package com.fasting.time.ui.fasting

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fasting.time.di.AppContainer
import com.fasting.time.domain.model.FastingPhase
import com.fasting.time.resources.Res
import com.fasting.time.resources.caption_eating
import com.fasting.time.resources.caption_fasting
import com.fasting.time.resources.caption_none
import com.fasting.time.resources.phase_eating
import com.fasting.time.resources.phase_fasting
import com.fasting.time.resources.phase_none
import com.fasting.time.resources.start_eating
import com.fasting.time.resources.start_fasting
import com.fasting.time.ui.format.toTimerText
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration

@Composable
fun FastingRoute(container: AppContainer, modifier: Modifier = Modifier) {
    val viewModel = viewModel {
        FastingViewModel(container.observeFastingTimer, container.startPhase)
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    FastingScreen(state = state, onStart = viewModel::start, modifier = modifier)
}

/** The timer in the middle of its tab, over the sign, and a button to start the next phase. */
@Composable
fun FastingScreen(
    state: FastingUiState,
    onStart: (FastingPhase) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Nothing until the saved phase has been read, so the first frame doesn't guess.
    if (state.isLoading) return
    Box(modifier = modifier.fillMaxSize().padding(24.dp)) {
        // The timer stays in the middle of the tab, over the sign, so it keeps clear
        // of the button by leaving its height free both below and above itself.
        var buttonHeight by remember { mutableIntStateOf(0) }
        val clearance = with(LocalDensity.current) { buttonHeight.toDp() } + 16.dp
        Readout(
            phase = state.phase,
            elapsed = state.elapsed,
            modifier = Modifier.align(Alignment.Center).padding(vertical = clearance),
        )
        // The one thing to do next: end a fast by eating, otherwise begin a fast. That
        // covers the first launch too, when nothing is under way yet.
        val next =
            if (state.phase == FastingPhase.Fasting) FastingPhase.Eating else FastingPhase.Fasting
        StartButton(
            text = stringResource(
                when (next) {
                    FastingPhase.Fasting -> Res.string.start_fasting
                    FastingPhase.Eating -> Res.string.start_eating
                },
            ),
            onClick = { onStart(next) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .onSizeChanged { buttonHeight = it.height },
        )
    }
}

@Composable
private fun Readout(phase: FastingPhase?, elapsed: Duration, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(
                when (phase) {
                    FastingPhase.Fasting -> Res.string.phase_fasting
                    FastingPhase.Eating -> Res.string.phase_eating
                    null -> Res.string.phase_none
                },
            ).uppercase(),
            style = ReadoutStyle.copy(fontSize = 20.sp, letterSpacing = 4.sp),
        )
        // As big as the space allows, so the same text fills a phone held either way.
        BasicText(
            text = elapsed.toTimerText(),
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
        Text(
            text = stringResource(
                when (phase) {
                    FastingPhase.Fasting -> Res.string.caption_fasting
                    FastingPhase.Eating -> Res.string.caption_eating
                    null -> Res.string.caption_none
                },
            ),
            style = ReadoutStyle.copy(fontSize = 18.sp),
        )
    }
}

/** White stays readable over both skies. */
@Composable
private fun StartButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier
            .widthIn(max = 360.dp)
            .fillMaxWidth()
            .defaultMinSize(minHeight = 64.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.onBackground,
            contentColor = MaterialTheme.colorScheme.background,
        ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        )
    }
}

private val ReadoutStyle = TextStyle(
    color = Color.White,
    fontWeight = FontWeight.Medium,
    // Keeps white text readable when a utensil or the bar of the sign passes behind it.
    shadow = Shadow(color = Color.Black.copy(alpha = 0.35f), blurRadius = 24f),
)
