package com.fasting.time.domain.model

import kotlin.time.Duration
import kotlin.time.Instant

/** The phase under way and the moment the user said it began. */
data class CurrentPhase(val phase: FastingPhase, val startedAt: Instant) {
    /** How long the phase has lasted at [now]. Never negative, even if the clock was set back. */
    fun elapsedAt(now: Instant): Duration = (now - startedAt).coerceAtLeast(Duration.ZERO)
}
