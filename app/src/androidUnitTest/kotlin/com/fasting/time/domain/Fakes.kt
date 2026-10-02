package com.fasting.time.domain

import com.fasting.time.data.phase.PhaseStore
import com.fasting.time.data.phase.SavedPhase
import com.fasting.time.data.session.SessionLogStore
import com.fasting.time.domain.repository.TimeZoneRepository
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

/** Friday 2 October 2026, half past seven in the evening in UTC. */
val Start = Instant.parse("2026-10-02T19:30:00Z")

class FakeClock(var now: Instant = Start) : Clock {
    override fun now(): Instant = now
}

class FakePhaseStore(private var saved: SavedPhase? = null) : PhaseStore {
    override fun read(): SavedPhase? = saved

    override fun write(phase: SavedPhase) {
        saved = phase
    }
}

class FakeSessionLogStore(var log: String? = null) : SessionLogStore {
    override fun read(): String? = log

    override fun write(log: String) {
        this.log = log
    }
}

class FixedTimeZoneRepository(private val utcOffset: Duration = Duration.ZERO) : TimeZoneRepository {
    override fun utcOffsetAt(instant: Instant): Duration = utcOffset
}
