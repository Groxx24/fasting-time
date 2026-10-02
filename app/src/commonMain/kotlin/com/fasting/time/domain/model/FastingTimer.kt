package com.fasting.time.domain.model

import kotlin.time.Duration

/** A reading of the timer: the phase under way and how long it has lasted so far. */
data class FastingTimer(val phase: FastingPhase, val elapsed: Duration)
