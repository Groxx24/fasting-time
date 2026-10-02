package com.fasting.time.ui.notification

import com.fasting.time.domain.model.FastingPhase
import com.fasting.time.domain.model.FastingTimer
import com.fasting.time.domain.model.FastingWindow
import com.fasting.time.domain.notification.PhaseNotifier
import platform.Foundation.NSDateComponents
import platform.Foundation.NSUserDefaults
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

/**
 * Shows the phase in two ways, because neither is enough alone. A Live Activity counts down on
 * the Lock Screen, but only the open app can start one and the system ends it after eight hours.
 * So each phase is also announced by a plain notification when it begins, which the system
 * repeats daily by itself and which waits in Notification Center until the user removes it.
 */
class LockScreenPhaseNotifier(private val liveActivity: PhaseLiveActivity) : PhaseNotifier {
    private val center = UNUserNotificationCenter.currentNotificationCenter()
    private val defaults = NSUserDefaults.standardUserDefaults

    override suspend fun show(timer: FastingTimer, endsAt: Instant) {
        showLiveActivity(timer, endsAt)
        scheduleNotifications(timer)
    }

    private suspend fun showLiveActivity(timer: FastingTimer, endsAt: Instant) {
        val now = endsAt - timer.remaining
        if (defaults.integerForKey(EndsAtKey) == endsAt.epochSeconds) {
            if (liveActivity.isShowing) return
            // It is gone sooner than the system would end it, so the user removed it, and it
            // stays away until the next phase.
            val startedAt = Instant.fromEpochSeconds(defaults.integerForKey(StartedAtKey))
            if (now - startedAt < SystemLimit) return
        }

        val window = timer.window
        val next = FastingPhase.entries.first { it != timer.phase }
        val nextLasts =
            if (next == FastingPhase.Fasting) window.fastingDuration else window.eatingDuration
        liveActivity.start(
            current = liveActivityPhase(timer.phase, window, endsAt),
            next = liveActivityPhase(next, window, endsAt + nextLasts),
        )
        defaults.setInteger(endsAt.epochSeconds, forKey = EndsAtKey)
        defaults.setInteger(now.epochSeconds, forKey = StartedAtKey)
    }

    private suspend fun liveActivityPhase(
        phase: FastingPhase,
        window: FastingWindow,
        endsAt: Instant,
    ): LiveActivityPhase {
        val text = phaseNotificationText(phase, window)
        return LiveActivityPhase(
            isFasting = phase == FastingPhase.Fasting,
            title = text.title,
            caption = text.body,
            endsAtEpochSeconds = endsAt.epochSeconds.toDouble(),
        )
    }

    private suspend fun scheduleNotifications(timer: FastingTimer) {
        // Asks only the first time; what is scheduled below starts arriving once allowed.
        center.requestAuthorizationWithOptions(UNAuthorizationOptionAlert) { _, _ -> }

        val window = timer.window
        for (phase in FastingPhase.entries) {
            val text = phaseNotificationText(phase, window)
            val content = UNMutableNotificationContent().apply {
                setTitle(text.title)
                setBody(text.body)
            }
            val begins = if (phase == FastingPhase.Fasting) window.start else window.end
            val time = NSDateComponents().apply {
                hour = begins.hour.toLong()
                minute = begins.minute.toLong()
            }
            // The phase's name as the identifier, so this takes the place of the one before.
            center.addNotificationRequest(
                UNNotificationRequest.requestWithIdentifier(
                    identifier = phase.name,
                    content = content,
                    trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
                        dateComponents = time,
                        repeats = true,
                    ),
                ),
                withCompletionHandler = null,
            )
        }
        // The app is open, so clear away what was said about the phase that is over.
        val over = FastingPhase.entries.filter { it != timer.phase }.map { it.name }
        center.removeDeliveredNotificationsWithIdentifiers(over)
    }

    private companion object {
        /** The end of the phase the Live Activity was last started for, and when that was. */
        const val EndsAtKey = "live_activity_phase_ends_at"
        const val StartedAtKey = "live_activity_started_at"

        /** How long the system lets a Live Activity run. */
        val SystemLimit = 8.hours
    }
}
