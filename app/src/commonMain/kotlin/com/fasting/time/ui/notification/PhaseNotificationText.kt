package com.fasting.time.ui.notification

import com.fasting.time.domain.model.FastingPhase
import com.fasting.time.domain.model.FastingWindow
import com.fasting.time.resources.Res
import com.fasting.time.resources.caption_eating
import com.fasting.time.resources.caption_fasting
import com.fasting.time.resources.countdown_eating
import com.fasting.time.resources.countdown_fasting
import com.fasting.time.ui.format.toClockText
import org.jetbrains.compose.resources.getString

/**
 * The words around the countdown: [label] says what the time left leads to, [caption] which
 * phase it is and until when. The caption also stands alone where no countdown can be shown.
 */
internal class PhaseNotificationText(val label: String, val caption: String)

/** What a notification says about [phase], in the same words as the timer screen. */
internal suspend fun phaseNotificationText(
    phase: FastingPhase,
    window: FastingWindow,
): PhaseNotificationText =
    when (phase) {
        FastingPhase.Fasting -> PhaseNotificationText(
            label = getString(Res.string.countdown_fasting),
            caption = getString(Res.string.caption_fasting, window.end.toClockText()),
        )
        FastingPhase.Eating -> PhaseNotificationText(
            label = getString(Res.string.countdown_eating),
            caption = getString(Res.string.caption_eating, window.start.toClockText()),
        )
    }
