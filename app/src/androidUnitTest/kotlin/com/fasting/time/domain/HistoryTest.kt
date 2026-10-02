package com.fasting.time.domain

import com.fasting.time.data.repository.StoredSessionRepository
import com.fasting.time.domain.model.Day
import com.fasting.time.domain.model.DaySummary
import com.fasting.time.domain.model.FastingPhase
import com.fasting.time.domain.model.History
import com.fasting.time.domain.model.HistoryGrouping
import com.fasting.time.domain.model.PeriodSummary
import com.fasting.time.domain.model.Session
import com.fasting.time.domain.usecase.ObserveHistoryUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

class HistoryTest {
    @Test
    fun aDay_keepsItsLongestFastAndAddsUpItsEating() = runBlocking {
        val history = historyOf(
            // Broken early for a snack, then picked up again until lunch.
            fast("2026-09-30T19:30:00Z", 13.hours),
            eating("2026-10-01T08:30:00Z", 1.hours),
            fast("2026-10-01T09:30:00Z", 2.hours),
            eating("2026-10-01T11:30:00Z", 8.hours),
        )

        assertEquals(
            listOf(DaySummary(day(2026, 10, 1), longestFast = 13.hours, eating = 9.hours)),
            history.periods.single().days,
        )
        assertEquals(13.hours, history.longestFast)
    }

    @Test
    fun aFast_countsForTheDayItEndedOn() = runBlocking {
        val history = historyOf(fast("2026-10-01T19:30:00Z", 16.hours))

        assertEquals(day(2026, 10, 2), history.periods.single().days.single().day)
    }

    @Test
    fun aDay_isTheLocalOne() = runBlocking {
        // Ends at 23:30 UTC, which is already the next day two hours east of it.
        val history = historyOf(fast("2026-10-01T09:30:00Z", 14.hours), utcOffset = 2.hours)

        assertEquals(day(2026, 10, 2), history.periods.single().days.single().day)
    }

    @Test
    fun weeks_runFromMondayAndComeNewestFirst() = runBlocking {
        val history = historyOf(
            fast("2026-09-26T19:30:00Z", 15.hours), // ends Sunday 27 September
            fast("2026-09-27T19:30:00Z", 16.hours), // ends Monday 28 September
            fast("2026-10-01T19:30:00Z", 18.hours), // ends Friday 2 October
        )

        assertEquals(
            listOf(
                PeriodSummary(
                    start = day(2026, 9, 28),
                    days = listOf(
                        DaySummary(day(2026, 10, 2), 18.hours, null),
                        DaySummary(day(2026, 9, 28), 16.hours, null),
                    ),
                ),
                PeriodSummary(
                    start = day(2026, 9, 21),
                    days = listOf(DaySummary(day(2026, 9, 27), 15.hours, null)),
                ),
            ),
            history.periods,
        )
        assertEquals(18.hours, history.periods.first().longestFast)
        assertEquals(17.hours, history.periods.first().averageFast)
        assertEquals(null, history.periods.first().averageEating)
    }

    @Test
    fun months_splitTheSameDaysDifferently() = runBlocking {
        val history = historyOf(
            fast("2026-09-26T19:30:00Z", 15.hours),
            fast("2026-09-27T19:30:00Z", 16.hours),
            fast("2026-10-01T19:30:00Z", 18.hours),
            grouping = HistoryGrouping.Month,
        )

        assertEquals(listOf(day(2026, 10, 1), day(2026, 9, 1)), history.periods.map { it.start })
        assertEquals(listOf(1, 2), history.periods.map { it.days.size })
    }

    @Test
    fun anEmptyLog_hasNoRecord() = runBlocking {
        assertEquals(History(day(2026, 10, 2), longestFast = null, periods = emptyList()), historyOf())
    }

    private suspend fun historyOf(
        vararg sessions: Session,
        grouping: HistoryGrouping = HistoryGrouping.Week,
        utcOffset: Duration = Duration.ZERO,
    ): History {
        val repository = StoredSessionRepository(FakeSessionLogStore())
        sessions.forEach { repository.add(it) }
        return ObserveHistoryUseCase(repository, FixedTimeZoneRepository(utcOffset), FakeClock())
            .invoke(grouping)
            .first()
    }

    private fun fast(start: String, length: Duration) = session(FastingPhase.Fasting, start, length)

    private fun eating(start: String, length: Duration) = session(FastingPhase.Eating, start, length)

    private fun session(phase: FastingPhase, start: String, length: Duration): Session =
        Instant.parse(start).let { Session(phase, it, it + length) }

    private fun day(year: Int, month: Int, dayOfMonth: Int) =
        Day(LocalDate.of(year, month, dayOfMonth).toEpochDay())
}
