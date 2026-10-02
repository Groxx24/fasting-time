package com.fasting.time.domain

import com.fasting.time.data.window.FastingWindowStore
import com.fasting.time.data.window.SavedWindow
import com.fasting.time.domain.repository.TimeZoneRepository
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

/** Friday 2 October 2026, half past seven in the evening in UTC. */
val Start = Instant.parse("2026-10-02T19:30:00Z")

class FakeClock(var now: Instant = Start) : Clock {
    override fun now(): Instant = now
}

class FakeFastingWindowStore(private var saved: SavedWindow? = null) : FastingWindowStore {
    override fun read(): SavedWindow? = saved

    override fun write(window: SavedWindow) {
        saved = window
    }
}

class FixedTimeZoneRepository(private val utcOffset: Duration = Duration.ZERO) : TimeZoneRepository {
    override fun utcOffsetAt(instant: Instant): Duration = utcOffset
}
