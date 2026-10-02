package com.fasting.time.ui.format

import com.fasting.time.domain.model.TimeOfDay
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
    fun toClockText_padsBothParts() {
        assertEquals("00:00", TimeOfDay(0, 0).toClockText())
        assertEquals("08:05", TimeOfDay(8, 5).toClockText())
        assertEquals("23:59", TimeOfDay(23, 59).toClockText())
    }
}
