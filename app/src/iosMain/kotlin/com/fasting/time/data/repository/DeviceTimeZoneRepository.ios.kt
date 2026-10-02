package com.fasting.time.data.repository

import com.fasting.time.domain.repository.TimeZoneRepository
import platform.Foundation.NSDate
import platform.Foundation.NSTimeZone
import platform.Foundation.dateWithTimeIntervalSince1970
import platform.Foundation.localTimeZone
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class DeviceTimeZoneRepository : TimeZoneRepository {
    override fun utcOffsetAt(instant: Instant): Duration {
        val date = NSDate.dateWithTimeIntervalSince1970(instant.epochSeconds.toDouble())
        return NSTimeZone.localTimeZone.secondsFromGMTForDate(date).seconds
    }
}
