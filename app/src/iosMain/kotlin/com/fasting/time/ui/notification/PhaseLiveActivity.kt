package com.fasting.time.ui.notification

/**
 * The Live Activity on the Lock Screen and in the Dynamic Island. Only Swift can reach the API
 * behind it, so it is implemented in `iosApp` and handed to `MainViewController`.
 */
interface PhaseLiveActivity {
    /** Whether one is up now. False once the user or the system has taken it away. */
    val isShowing: Boolean

    /** Puts [current] up in place of whatever was there, to turn into [next] when it is over. */
    fun start(current: LiveActivityPhase, next: LiveActivityPhase)
}

/**
 * One phase as the Live Activity shows it: the [title] above the countdown, the [caption] below
 * it and the moment it reaches zero.
 */
class LiveActivityPhase(
    val isFasting: Boolean,
    val title: String,
    val caption: String,
    val endsAtEpochSeconds: Double,
)
