package com.fasting.time.di

import com.fasting.time.data.phase.PhaseStore
import com.fasting.time.data.repository.StoredFastingRepository
import com.fasting.time.data.repository.StoredSessionRepository
import com.fasting.time.data.session.SessionLogStore
import com.fasting.time.domain.repository.FastingRepository
import com.fasting.time.domain.repository.SessionRepository
import com.fasting.time.domain.repository.TimeZoneRepository
import com.fasting.time.domain.usecase.ObserveFastingTimerUseCase
import com.fasting.time.domain.usecase.ObserveHistoryUseCase
import com.fasting.time.domain.usecase.StartPhaseUseCase
import kotlin.time.Clock

/**
 * The one place where dependencies are wired together, by hand until a DI library is chosen.
 * Each platform creates a single instance and hands in what only it can build.
 */
class AppContainer(
    phaseStore: PhaseStore,
    sessionLogStore: SessionLogStore,
    timeZoneRepository: TimeZoneRepository,
) {
    private val clock: Clock = Clock.System
    private val fastingRepository: FastingRepository by lazy { StoredFastingRepository(phaseStore) }
    private val sessionRepository: SessionRepository by lazy {
        StoredSessionRepository(sessionLogStore)
    }

    val observeFastingTimer by lazy { ObserveFastingTimerUseCase(fastingRepository, clock) }
    val startPhase by lazy { StartPhaseUseCase(fastingRepository, sessionRepository, clock) }
    val observeHistory by lazy {
        ObserveHistoryUseCase(sessionRepository, timeZoneRepository, clock)
    }
}
