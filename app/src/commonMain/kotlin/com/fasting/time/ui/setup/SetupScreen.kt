package com.fasting.time.ui.setup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fasting.time.domain.model.FastingWindow
import com.fasting.time.domain.model.TimeOfDay
import com.fasting.time.resources.Res
import com.fasting.time.resources.setup_confirm
import com.fasting.time.resources.setup_end
import com.fasting.time.resources.setup_explanation
import com.fasting.time.resources.setup_start
import com.fasting.time.resources.setup_summary
import com.fasting.time.resources.setup_title
import com.fasting.time.ui.components.panel
import com.fasting.time.ui.format.toHoursMinutesText
import org.jetbrains.compose.resources.stringResource

/**
 * Asks for the window to fast in every day, starting from [initial]. The times being typed are
 * this screen's own state; nothing is chosen until [onConfirm].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    initial: FastingWindow,
    onConfirm: (FastingWindow) -> Unit,
    modifier: Modifier = Modifier,
) {
    val start = rememberTimePickerState(initial.start.hour, initial.start.minute, is24Hour = true)
    val end = rememberTimePickerState(initial.end.hour, initial.end.minute, is24Hour = true)
    val window = FastingWindow(start.toTimeOfDay(), end.toTimeOfDay())

    Column(
        // Scrolls when the keyboard or a phone held sideways leaves too little height.
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 360.dp)
                .fillMaxWidth()
                .panel(RoundedCornerShape(28.dp))
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(Res.string.setup_title),
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
            )
            Text(
                text = stringResource(Res.string.setup_explanation),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(Res.string.setup_start),
                modifier = Modifier.padding(top = 12.dp),
                style = MaterialTheme.typography.titleMedium,
            )
            TimeInput(state = start, colors = timeInputColors())
            Text(
                text = stringResource(Res.string.setup_end),
                style = MaterialTheme.typography.titleMedium,
            )
            TimeInput(state = end, colors = timeInputColors())
            Text(
                text = stringResource(
                    Res.string.setup_summary,
                    window.fastingDuration.toHoursMinutesText(),
                    window.eatingDuration.toHoursMinutesText(),
                ),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        // White stays readable over both skies.
        Button(
            onClick = { onConfirm(window) },
            modifier = Modifier
                .widthIn(max = 360.dp)
                .fillMaxWidth()
                .defaultMinSize(minHeight = 64.dp),
            // The same time twice would be a fast of no length.
            enabled = window.start != window.end,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onBackground,
                contentColor = MaterialTheme.colorScheme.background,
            ),
        ) {
            Text(
                text = stringResource(Res.string.setup_confirm),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
private fun TimePickerState.toTimeOfDay() = TimeOfDay(hour, minute)

/** White on see-through white, like the panel the fields sit on, in place of the Material tints. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun timeInputColors() = TimePickerDefaults.colors(
    timeSelectorSelectedContainerColor = Color.White.copy(alpha = 0.18f),
    timeSelectorUnselectedContainerColor = Color.White.copy(alpha = 0.08f),
    timeSelectorSelectedContentColor = Color.White,
    timeSelectorUnselectedContentColor = Color.White,
)
