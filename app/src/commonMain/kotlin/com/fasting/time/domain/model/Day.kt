package com.fasting.time.domain.model

import kotlin.time.Duration
import kotlin.time.Instant

/** A calendar day, counted in days since 1 January 1970. */
data class Day(val epochDay: Long) : Comparable<Day> {
    val year: Int get() = date().year

    /** From 1 for January to 12 for December. */
    val month: Int get() = date().month

    val dayOfMonth: Int get() = date().dayOfMonth

    /** From 0 for Monday to 6 for Sunday. */
    val dayOfWeek: Int get() = (epochDay + 3).mod(7)

    /** The Monday of this day's week. */
    val weekStart: Day get() = Day(epochDay - dayOfWeek)

    /** The first day of this day's month. */
    val monthStart: Day get() = Day(epochDay - (dayOfMonth - 1))

    operator fun plus(days: Int): Day = Day(epochDay + days)

    override fun compareTo(other: Day): Int = epochDay.compareTo(other.epochDay)

    /** Howard Hinnant's days-to-civil algorithm, which counts in 400-year eras from 1 March. */
    private fun date(): Date {
        val shifted = epochDay + 719_468
        val era = shifted.floorDiv(146_097)
        val dayOfEra = shifted - era * 146_097
        val yearOfEra = (dayOfEra - dayOfEra / 1_460 + dayOfEra / 36_524 - dayOfEra / 146_096) / 365
        val dayOfYear = dayOfEra - (365 * yearOfEra + yearOfEra / 4 - yearOfEra / 100)
        val monthFromMarch = (5 * dayOfYear + 2) / 153
        val month = if (monthFromMarch < 10) monthFromMarch + 3 else monthFromMarch - 9
        return Date(
            year = (yearOfEra + era * 400 + if (month <= 2) 1 else 0).toInt(),
            month = month.toInt(),
            dayOfMonth = (dayOfYear - (153 * monthFromMarch + 2) / 5 + 1).toInt(),
        )
    }

    private class Date(val year: Int, val month: Int, val dayOfMonth: Int)

    companion object {
        /** The day it is at [instant] where local time is [utcOffset] ahead of UTC. */
        fun of(instant: Instant, utcOffset: Duration): Day =
            Day((instant + utcOffset).epochSeconds.floorDiv(SecondsPerDay))

        private const val SecondsPerDay = 86_400L
    }
}
