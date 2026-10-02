package com.fasting.time.di

import com.fasting.time.data.repository.StoredFastingWindowRepository
import com.fasting.time.data.window.FastingWindowStore
import com.fasting.time.domain.notification.PhaseNotifier
import com.fasting.time.domain.repository.FastingWindowRepository
import com.fasting.time.domain.repository.TimeZoneRepository
import com.fasting.time.domain.usecase.ObserveFastingTimerUseCase
import com.fasting.time.domain.usecase.SetFastingWindowUseCase
import com.fasting.time.domain.usecase.ShowPhaseNotificationUseCase
import kotlin.time.Clock

/**
 * The one place where dependencies are wired together, by hand until a DI library is chosen.
 * Each platform creates a single instance and hands in what only it can build.
 */
class AppContainer(
    fastingWindowStore: FastingWindowStore,
    phaseNotifier: PhaseNotifier,
    timeZoneRepository: TimeZoneRepository,
) {
    private val clock: Clock = Clock.System
    private val fastingWindowRepository: FastingWindowRepository by lazy {
        StoredFastingWindowRepository(fastingWindowStore)
    }

    val observeFastingTimer by lazy {
        ObserveFastingTimerUseCase(fastingWindowRepository, timeZoneRepository, clock)
    }
    val setFastingWindow by lazy { SetFastingWindowUseCase(fastingWindowRepository) }
    val showPhaseNotification by lazy {
        ShowPhaseNotificationUseCase(fastingWindowRepository, timeZoneRepository, phaseNotifier, clock)
    }
}
