package com.fasting.time.ui.notification

import com.fasting.time.domain.model.FastingPhase
import com.fasting.time.domain.model.FastingWindow
import com.fasting.time.resources.Res
import com.fasting.time.resources.caption_eating
import com.fasting.time.resources.caption_fasting
import com.fasting.time.resources.phase_eating
import com.fasting.time.resources.phase_fasting
import com.fasting.time.ui.format.toClockText
import org.jetbrains.compose.resources.getString

internal class PhaseNotificationText(val title: String, val body: String)

/** What a notification says about [phase], in the same words as the timer screen. */
internal suspend fun phaseNotificationText(
    phase: FastingPhase,
    window: FastingWindow,
): PhaseNotificationText =
    when (phase) {
        FastingPhase.Fasting -> PhaseNotificationText(
            title = getString(Res.string.phase_fasting),
            body = getString(Res.string.caption_fasting, window.end.toClockText()),
        )
        FastingPhase.Eating -> PhaseNotificationText(
            title = getString(Res.string.phase_eating),
            body = getString(Res.string.caption_eating, window.start.toClockText()),
        )
    }
