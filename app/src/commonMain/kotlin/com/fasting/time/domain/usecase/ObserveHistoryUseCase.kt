package com.fasting.time.domain.usecase

import com.fasting.time.domain.model.Day
import com.fasting.time.domain.model.DaySummary
import com.fasting.time.domain.model.FastingPhase
import com.fasting.time.domain.model.History
import com.fasting.time.domain.model.HistoryGrouping
import com.fasting.time.domain.model.PeriodSummary
import com.fasting.time.domain.model.Session
import com.fasting.time.domain.repository.SessionRepository
import com.fasting.time.domain.repository.TimeZoneRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

/** Sums the log up day by day, and puts the days together into weeks or months. */
class ObserveHistoryUseCase(
    private val sessionRepository: SessionRepository,
    private val timeZoneRepository: TimeZoneRepository,
    private val clock: Clock,
) {
    operator fun invoke(grouping: HistoryGrouping): Flow<History> =
        sessionRepository.observe().map { sessions ->
            History(
                today = dayOf(clock.now()),
                longestFast = sessions.durationsOf(FastingPhase.Fasting).maxOrNull(),
                periods = sessions
                    // A fast runs over midnight; it belongs to the day it was finished on.
                    .groupBy { dayOf(it.endedAt) }
                    .map { (day, ended) ->
                        DaySummary(
                            day = day,
                            longestFast = ended.durationsOf(FastingPhase.Fasting).maxOrNull(),
                            eating = ended.durationsOf(FastingPhase.Eating)
                                .reduceOrNull(Duration::plus),
                        )
                    }
                    .sortedByDescending { it.day }
                    .groupBy { it.day.periodStart(grouping) }
                    .map { (start, days) -> PeriodSummary(start, days) },
            )
        }

    private fun dayOf(instant: Instant): Day =
        Day.of(instant, timeZoneRepository.utcOffsetAt(instant))

    private fun List<Session>.durationsOf(phase: FastingPhase): List<Duration> =
        filter { it.phase == phase }.map { it.duration }

    private fun Day.periodStart(grouping: HistoryGrouping): Day =
        when (grouping) {
            HistoryGrouping.Week -> weekStart
            HistoryGrouping.Month -> monthStart
        }
}
