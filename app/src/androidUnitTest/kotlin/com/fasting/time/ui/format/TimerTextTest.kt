package com.fasting.time.ui.format

import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class TimerTextTest {
    @Test
    fun toTimerText_padsEveryPart() {
        assertEquals("00:00:00", Duration.ZERO.toTimerText())
        assertEquals("01:02:03", (1.hours + 2.minutes + 3.seconds).toTimerText())
        assertEquals("16:00:59", (16.hours + 59.seconds + 999.milliseconds).toTimerText())
    }

    @Test
    fun toTimerText_keepsCountingHoursPastADay() {
        assertEquals("36:05:00", (36.hours + 5.minutes).toTimerText())
    }
}
