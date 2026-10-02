package com.fasting.time.domain.model

import kotlin.time.Duration
import kotlin.time.Instant

/** A finished phase: which one it was, and when it began and ended. */
data class Session(val phase: FastingPhase, val startedAt: Instant, val endedAt: Instant) {
    val duration: Duration get() = (endedAt - startedAt).coerceAtLeast(Duration.ZERO)
}
