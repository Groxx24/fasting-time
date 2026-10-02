package com.fasting.time.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

class DayTest {
    @Test
    fun day_agreesWithTheCalendar() {
        // Every day of six decades, leap years and the year 2000 among them.
        val first = LocalDate.of(1968, 1, 1).toEpochDay()
        val last = LocalDate.of(2032, 12, 31).toEpochDay()
        for (epochDay in first..last) {
            val expected = LocalDate.ofEpochDay(epochDay)
            val day = Day(epochDay)

            assertEquals(expected.year, day.year)
            assertEquals(expected.monthValue, day.month)
            assertEquals(expected.dayOfMonth, day.dayOfMonth)
            assertEquals(expected.dayOfWeek.value - 1, day.dayOfWeek)
        }
    }

    @Test
    fun weekStart_isTheMondayBefore() {
        val friday = Day(LocalDate.of(2026, 10, 2).toEpochDay())
        val monday = Day(LocalDate.of(2026, 9, 28).toEpochDay())

        assertEquals(monday, friday.weekStart)
        assertEquals(monday, monday.weekStart)
        assertEquals(monday, (monday + 6).weekStart)
    }

    @Test
    fun monthStart_isTheFirstOfTheMonth() {
        val first = Day(LocalDate.of(2026, 10, 1).toEpochDay())

        assertEquals(first, (first + 30).monthStart)
        assertEquals(first, first.monthStart)
    }

    @Test
    fun of_followsLocalTimeNotUtc() {
        val lateEvening = Instant.parse("2026-10-02T23:30:00Z")
        val second = Day(LocalDate.of(2026, 10, 2).toEpochDay())

        assertEquals(second, Day.of(lateEvening, utcOffset = 0.hours))
        assertEquals(second + 1, Day.of(lateEvening, utcOffset = 2.hours))
        assertEquals(
            second + -1,
            Day.of(Instant.parse("2026-10-02T03:00:00Z"), utcOffset = (-5).hours),
        )
    }
}
