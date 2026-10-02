package com.fasting.time.domain.notification

import com.fasting.time.domain.model.FastingTimer
import kotlin.time.Instant

/** Tells the user which phase it is without the app being open. Each platform has its own. */
interface PhaseNotifier {
    /**
     * Shows the phase of [timer], which lasts until [endsAt], and sees to it that the phases
     * after it are shown as they come.
     */
    suspend fun show(timer: FastingTimer, endsAt: Instant)
}
