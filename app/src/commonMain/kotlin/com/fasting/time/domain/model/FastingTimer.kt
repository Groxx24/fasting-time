package com.fasting.time.domain.model

import kotlin.time.Duration

/** A reading of the timer: the phase it is by the [window], and how long until the other one. */
data class FastingTimer(val phase: FastingPhase, val remaining: Duration, val window: FastingWindow)
