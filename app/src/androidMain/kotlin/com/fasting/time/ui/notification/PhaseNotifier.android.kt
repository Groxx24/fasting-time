package com.fasting.time.ui.notification

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.fasting.time.FastingTimeApplication
import com.fasting.time.MainActivity
import com.fasting.time.R
import com.fasting.time.domain.model.FastingTimer
import com.fasting.time.domain.notification.PhaseNotifier
import com.fasting.time.resources.Res
import com.fasting.time.resources.notification_channel
import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.getString
import kotlin.time.Instant

/**
 * One notification that counts down the phase and goes when it ends. An alarm set for that moment
 * brings [PhaseNotificationReceiver] to show the next one, so nothing runs in between.
 */
class SystemPhaseNotifier(private val context: Context) : PhaseNotifier {
    private val notifications = context.getSystemService(NotificationManager::class.java)
    private val alarms = context.getSystemService(AlarmManager::class.java)

    override suspend fun show(timer: FastingTimer, endsAt: Instant) {
        val endsAtMillis = endsAt.toEpochMilliseconds()
        // Not exact, which would need a permission: the next phase may show a few minutes late.
        alarms.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            endsAtMillis,
            broadcast(Intent(context, PhaseNotificationReceiver::class.java).setAction(ActionRefresh)),
        )
        // The user removed the notification of this phase, so it stays away until the next one.
        if (removals(context).getLong(RemovedKey, 0) == endsAtMillis) {
            // One may be left from before the clock or the window changed back to this phase.
            notifications.cancel(NotificationId)
            return
        }

        notifications.createNotificationChannel(
            // Low importance: it is there to be looked at, not to make a sound.
            NotificationChannel(
                ChannelId,
                getString(Res.string.notification_channel),
                NotificationManager.IMPORTANCE_LOW,
            ),
        )
        val text = phaseNotificationText(timer.phase, timer.window)
        val notification = Notification.Builder(context, ChannelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(text.caption)
            // In the header, right before the countdown, so the two read as one line.
            .setSubText(text.label)
            .setWhen(endsAtMillis)
            .setUsesChronometer(true)
            .setChronometerCountDown(true)
            .setTimeoutAfter(timer.remaining.inWholeMilliseconds)
            // Stays through "Clear all" where the user can still swipe it away, which is from
            // Android 14. Before that an ongoing notification could not be removed at all.
            .setOngoing(Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
            .setContentIntent(
                PendingIntent.getActivity(
                    context,
                    0,
                    Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE,
                ),
            )
            .setDeleteIntent(
                broadcast(
                    Intent(context, PhaseNotificationReceiver::class.java)
                        .setAction(ActionRemoved)
                        .putExtra(EndsAtExtra, endsAtMillis),
                ),
            )
            .build()
        notifications.notify(NotificationId, notification)
    }

    private fun broadcast(intent: Intent): PendingIntent =
        PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    private companion object {
        const val ChannelId = "phase"
        const val NotificationId = 1
    }
}

/**
 * Woken by the alarm at the end of a phase, and by the system after a restart or a change of the
 * clock, to show the phase it is now. Also told when the user removes the notification.
 */
class PhaseNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ActionRemoved) {
            removals(context).edit()
                .putLong(RemovedKey, intent.getLongExtra(EndsAtExtra, 0))
                .apply()
        } else {
            val container = (context.applicationContext as FastingTimeApplication).container
            // Quick enough to finish within the receiver: one read of the store and a notify.
            runBlocking { container.showPhaseNotification() }
        }
    }
}

/** Remembers the end of the phase whose notification the user removed, in milliseconds. */
private fun removals(context: Context) =
    context.getSharedPreferences("phase_notification", Context.MODE_PRIVATE)

private const val RemovedKey = "removed_phase_ends_at"
private const val EndsAtExtra = "ends_at"
private const val ActionRefresh = "com.fasting.time.action.REFRESH_PHASE_NOTIFICATION"
private const val ActionRemoved = "com.fasting.time.action.PHASE_NOTIFICATION_REMOVED"
