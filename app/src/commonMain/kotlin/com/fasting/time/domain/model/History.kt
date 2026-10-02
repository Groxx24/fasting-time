package com.fasting.time.domain.model

import kotlin.time.Duration

/** How the days of the history are put together into periods. */
enum class HistoryGrouping {
    /** Monday to Sunday. */
    Week,
    Month,
}

/** What was finished on one day. A phase counts for the day it ended on. */
data class DaySummary(
    val day: Day,
    /** The longest fast that ended this day, so a fast broken early doesn't hide a longer one. */
    val longestFast: Duration?,
    /** All the eating that ended this day, added up. */
    val eating: Duration?,
)

/** The days of one week or month that have something logged, newest first. */
data class PeriodSummary(val start: Day, val days: List<DaySummary>) {
    val longestFast: Duration? get() = days.mapNotNull { it.longestFast }.maxOrNull()

    /** The average over the days that had a fast, taking each day's longest. */
    val averageFast: Duration? get() = days.mapNotNull { it.longestFast }.averageOrNull()

    /** The average over the days that had any eating. */
    val averageEating: Duration? get() = days.mapNotNull { it.eating }.averageOrNull()
}

data class History(
    val today: Day,
    /** The longest fast ever finished: the record to beat. */
    val longestFast: Duration?,
    /** Newest first. */
    val periods: List<PeriodSummary>,
)

private fun List<Duration>.averageOrNull(): Duration? =
    if (isEmpty()) null else fold(Duration.ZERO, Duration::plus) / size
