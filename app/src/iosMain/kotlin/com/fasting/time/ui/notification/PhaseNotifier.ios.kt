package com.fasting.time.ui.notification

import com.fasting.time.domain.model.FastingPhase
import com.fasting.time.domain.model.FastingTimer
import com.fasting.time.domain.notification.PhaseNotifier
import platform.Foundation.NSDateComponents
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.time.Instant

/**
 * iOS has no notification that stays and updates by itself, so each phase is announced by one
 * that arrives when it begins and waits in Notification Center until the user removes it. Both
 * repeat daily on the system's own schedule, so nothing runs in between.
 */
class UserNotificationsPhaseNotifier : PhaseNotifier {
    private val center = UNUserNotificationCenter.currentNotificationCenter()

    override suspend fun show(timer: FastingTimer, endsAt: Instant) {
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
}
