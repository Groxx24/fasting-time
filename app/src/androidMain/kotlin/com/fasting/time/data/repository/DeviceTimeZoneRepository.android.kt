package com.fasting.time.data.repository

import com.fasting.time.domain.repository.TimeZoneRepository
import java.util.TimeZone
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Instant

class DeviceTimeZoneRepository : TimeZoneRepository {
    override fun utcOffsetAt(instant: Instant): Duration =
        TimeZone.getDefault().getOffset(instant.toEpochMilliseconds()).milliseconds
}
