package com.fasting.time.domain.model

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/** A time on the clock, counted in minutes since midnight. */
data class TimeOfDay(val minuteOfDay: Int) {
    constructor(hour: Int, minute: Int) : this(hour * 60 + minute)

    val hour: Int get() = minuteOfDay / 60
    val minute: Int get() = minuteOfDay % 60
    val sinceMidnight: Duration get() = minuteOfDay.minutes
}
