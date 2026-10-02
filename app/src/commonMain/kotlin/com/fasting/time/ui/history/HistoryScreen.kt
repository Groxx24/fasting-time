package com.fasting.time.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fasting.time.di.AppContainer
import com.fasting.time.domain.model.Day
import com.fasting.time.domain.model.DaySummary
import com.fasting.time.domain.model.HistoryGrouping
import com.fasting.time.domain.model.PeriodSummary
import com.fasting.time.resources.Res
import com.fasting.time.resources.column_eating
import com.fasting.time.resources.column_fasting
import com.fasting.time.resources.day_and_month
import com.fasting.time.resources.duration_none
import com.fasting.time.resources.grouping_month
import com.fasting.time.resources.grouping_week
import com.fasting.time.resources.history_empty
import com.fasting.time.resources.month_names
import com.fasting.time.resources.month_names_short
import com.fasting.time.resources.period_last_week
import com.fasting.time.resources.period_month
import com.fasting.time.resources.period_this_week
import com.fasting.time.resources.period_week_range
import com.fasting.time.resources.record_beaten
import com.fasting.time.resources.record_current
import com.fasting.time.resources.record_none
import com.fasting.time.resources.record_title
import com.fasting.time.resources.record_to_go
import com.fasting.time.resources.stat_average_eating
import com.fasting.time.resources.stat_average_fast
import com.fasting.time.resources.stat_longest_fast
import com.fasting.time.resources.weekday_and_day
import com.fasting.time.resources.weekday_names_short
import com.fasting.time.ui.components.PillSelector
import com.fasting.time.ui.components.ProgressBar
import com.fasting.time.ui.components.panel
import com.fasting.time.ui.format.toHoursMinutesText
import com.fasting.time.ui.theme.EatingTint
import com.fasting.time.ui.theme.FastingTint
import org.jetbrains.compose.resources.stringArrayResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration

@Composable
fun HistoryRoute(container: AppContainer, modifier: Modifier = Modifier) {
    val viewModel = viewModel {
        HistoryViewModel(container.observeHistory, container.observeFastingTimer)
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    HistoryScreen(state = state, onGroupingChange = viewModel::setGrouping, modifier = modifier)
}

/** The record to beat on top, then every week or month that has something logged. */
@Composable
fun HistoryScreen(
    state: HistoryUiState,
    onGroupingChange: (HistoryGrouping) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (state.isLoading) return
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item(key = "record") {
            RecordCard(longestFast = state.longestFast, currentFast = state.currentFast)
        }
        if (state.periods.isEmpty()) {
            item(key = "empty") {
                Text(
                    text = stringResource(Res.string.history_empty),
                    modifier = Modifier.padding(24.dp),
                    textAlign = TextAlign.Center,
                    style = LabelStyle.copy(fontSize = 16.sp),
                )
            }
        } else {
            item(key = "grouping") {
                PillSelector(
                    options = HistoryGrouping.entries,
                    selected = state.grouping,
                    onSelect = onGroupingChange,
                ) { grouping ->
                    Text(
                        text = stringResource(
                            when (grouping) {
                                HistoryGrouping.Week -> Res.string.grouping_week
                                HistoryGrouping.Month -> Res.string.grouping_month
                            },
                        ),
                        style = ValueStyle.copy(fontSize = 14.sp),
                    )
                }
            }
            items(state.periods, key = { it.start.epochDay }) { period ->
                PeriodCard(
                    period = period,
                    title = periodTitle(period.start, state.grouping, state.today),
                    // Every bar is measured against the record, so the weeks compare.
                    longestFast = state.longestFast,
                )
            }
        }
    }
}

/**
 * The longest fast ever, and how the fast under way compares to it. Once that one is longer it
 * takes the record's place, still counting.
 */
@Composable
private fun RecordCard(longestFast: Duration?, currentFast: Duration?) {
    val isBeaten = currentFast != null && longestFast != null && currentFast > longestFast
    val shown = if (isBeaten) currentFast else longestFast

    Column(modifier = Modifier.card(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = stringResource(Res.string.record_title).uppercase(), style = LabelStyle)
        Text(
            text = shown?.toHoursMinutesText() ?: stringResource(Res.string.duration_none),
            style = ValueStyle.copy(fontSize = 44.sp, fontWeight = FontWeight.Black),
        )
        if (longestFast == null) {
            Text(text = stringResource(Res.string.record_none), style = LabelStyle)
        }
        if (currentFast != null) {
            if (longestFast != null) {
                ProgressBar(
                    fraction = (currentFast / longestFast).toFloat(),
                    color = FastingTint,
                    modifier = Modifier.padding(vertical = 8.dp).fillMaxWidth().height(10.dp),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(
                        Res.string.record_current,
                        currentFast.toHoursMinutesText(),
                    ),
                    style = ValueStyle.copy(fontSize = 14.sp),
                )
                if (longestFast != null) {
                    Text(
                        text = if (isBeaten) {
                            stringResource(Res.string.record_beaten)
                        } else {
                            stringResource(
                                Res.string.record_to_go,
                                (longestFast - currentFast).toHoursMinutesText(),
                            )
                        },
                        style = LabelStyle.copy(fontSize = 14.sp),
                    )
                }
            }
        }
    }
}

@Composable
private fun PeriodCard(period: PeriodSummary, title: String, longestFast: Duration?) {
    Column(modifier = Modifier.card(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = title, style = ValueStyle.copy(fontSize = 20.sp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Stat(stringResource(Res.string.stat_longest_fast), period.longestFast, FastingTint)
            Stat(stringResource(Res.string.stat_average_fast), period.averageFast, FastingTint)
            Stat(stringResource(Res.string.stat_average_eating), period.averageEating, EatingTint)
        }
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row {
                Spacer(modifier = Modifier.weight(1f))
                ColumnLabel(stringResource(Res.string.column_fasting))
                ColumnLabel(stringResource(Res.string.column_eating))
            }
            period.days.forEach { day -> DayRow(day = day, longestFast = longestFast) }
        }
    }
}

@Composable
private fun RowScope.Stat(
    label: String,
    value: Duration?,
    color: Color,
) {
    Column(modifier = Modifier.weight(1f)) {
        Text(
            text = value?.toHoursMinutesText() ?: stringResource(Res.string.duration_none),
            style = ValueStyle.copy(fontSize = 20.sp, color = color),
        )
        Text(text = label, style = LabelStyle)
    }
}

@Composable
private fun ColumnLabel(text: String) {
    Text(
        text = text,
        modifier = Modifier.width(DurationColumnWidth),
        textAlign = TextAlign.End,
        style = LabelStyle,
    )
}

/** One day: its date, its longest fast as a bar against the record and a figure, its eating. */
@Composable
private fun DayRow(day: DaySummary, longestFast: Duration?) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(
                Res.string.weekday_and_day,
                stringArrayResource(Res.array.weekday_names_short)[day.day.dayOfWeek],
                day.day.dayOfMonth.toString(),
            ),
            modifier = Modifier.width(60.dp),
            style = ValueStyle.copy(fontSize = 14.sp, fontWeight = FontWeight.Medium),
        )
        ProgressBar(
            fraction = when {
                day.longestFast == null || longestFast == null -> 0f
                else -> (day.longestFast / longestFast).toFloat()
            },
            color = FastingTint,
            modifier = Modifier.weight(1f).height(8.dp),
        )
        DurationCell(day.longestFast, FastingTint)
        DurationCell(day.eating, EatingTint)
    }
}

@Composable
private fun DurationCell(value: Duration?, color: Color) {
    Text(
        text = value?.toHoursMinutesText() ?: stringResource(Res.string.duration_none),
        modifier = Modifier.width(DurationColumnWidth),
        textAlign = TextAlign.End,
        style = ValueStyle.copy(fontSize = 14.sp, color = color),
    )
}

/** "This week", "Last week" or the week's first and last day; for a month, its name and year. */
@Composable
private fun periodTitle(start: Day, grouping: HistoryGrouping, today: Day): String =
    when (grouping) {
        HistoryGrouping.Week -> when (start) {
            today.weekStart -> stringResource(Res.string.period_this_week)
            today.weekStart + -7 -> stringResource(Res.string.period_last_week)
            else -> stringResource(
                Res.string.period_week_range,
                dayAndMonth(start),
                dayAndMonth(start + 6),
            )
        }

        HistoryGrouping.Month -> stringResource(
            Res.string.period_month,
            stringArrayResource(Res.array.month_names)[start.month - 1],
            start.year.toString(),
        )
    }

@Composable
private fun dayAndMonth(day: Day): String =
    stringResource(
        Res.string.day_and_month,
        day.dayOfMonth.toString(),
        stringArrayResource(Res.array.month_names_short)[day.month - 1],
    )

/** Cards stop growing on a wide screen, where a full-width row would be hard to read across. */
private fun Modifier.card(): Modifier =
    widthIn(max = 520.dp)
        .fillMaxWidth()
        .panel(RoundedCornerShape(28.dp))
        .padding(20.dp)

private val DurationColumnWidth = 68.dp

private val ValueStyle = TextStyle(
    color = Color.White,
    fontWeight = FontWeight.Bold,
    // Same-width digits, so the figures line up down a column.
    fontFeatureSettings = "tnum",
)

private val LabelStyle = TextStyle(
    color = Color.White.copy(alpha = 0.7f),
    fontSize = 12.sp,
    fontWeight = FontWeight.Medium,
    letterSpacing = 0.5.sp,
)
