package com.fasting.time.domain.usecase

import com.fasting.time.domain.notification.PhaseNotifier
import com.fasting.time.domain.repository.FastingWindowRepository
import com.fasting.time.domain.repository.TimeZoneRepository
import kotlinx.coroutines.flow.first
import kotlin.time.Clock

/** Puts the phase it is now in a notification. Does nothing while no window has been chosen. */
class ShowPhaseNotificationUseCase(
    private val fastingWindowRepository: FastingWindowRepository,
    private val timeZoneRepository: TimeZoneRepository,
    private val phaseNotifier: PhaseNotifier,
    private val clock: Clock,
) {
    suspend operator fun invoke() {
        val window = fastingWindowRepository.observe().first() ?: return
        val now = clock.now()
        val timer = window.timerAt(now, timeZoneRepository.utcOffsetAt(now))
        phaseNotifier.show(timer, endsAt = now + timer.remaining)
    }
}
