package com.fasting.time.domain.repository

import kotlin.time.Duration
import kotlin.time.Instant

interface TimeZoneRepository {
    /** How far the device's local time was ahead of UTC at [instant]; negative when behind. */
    fun utcOffsetAt(instant: Instant): Duration
}
