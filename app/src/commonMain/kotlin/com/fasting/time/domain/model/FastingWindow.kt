package com.fasting.time.domain.model

import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

/**
 * The part of every day the user means to fast in, from [start] until [end]. It runs past
 * midnight when it ends earlier on the clock than it starts.
 */
data class FastingWindow(val start: TimeOfDay, val end: TimeOfDay) {
    val fastingDuration: Duration
        get() = (end.sinceMidnight - start.sinceMidnight).wrappedIntoDay()

    val eatingDuration: Duration get() = 1.days - fastingDuration

    /**
     * The phase it is at [now] where local time is [utcOffset] ahead of UTC, and how much of it
     * is left.
     */
    fun timerAt(now: Instant, utcOffset: Duration): FastingTimer {
        val sinceMidnight = (now + utcOffset).toEpochMilliseconds().mod(MillisPerDay).milliseconds
        val sinceStart = (sinceMidnight - start.sinceMidnight).wrappedIntoDay()
        return if (sinceStart < fastingDuration) {
            FastingTimer(FastingPhase.Fasting, fastingDuration - sinceStart, this)
        } else {
            FastingTimer(FastingPhase.Eating, 1.days - sinceStart, this)
        }
    }

    private fun Duration.wrappedIntoDay(): Duration = if (isNegative()) this + 1.days else this

    companion object {
        /** What the first screen offers: sixteen hours from eight in the evening until noon. */
        val Default = FastingWindow(TimeOfDay(20, 0), TimeOfDay(12, 0))

        private const val MillisPerDay = 86_400_000L
    }
}
