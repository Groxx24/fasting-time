package com.fasting.time.domain.usecase

import com.fasting.time.domain.model.CurrentPhase
import com.fasting.time.domain.model.FastingPhase
import com.fasting.time.domain.repository.FastingRepository
import kotlin.time.Clock

/** Starts a phase now, which ends the one that was under way and restarts the timer. */
class StartPhaseUseCase(
    private val fastingRepository: FastingRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(phase: FastingPhase) {
        fastingRepository.save(CurrentPhase(phase, clock.now()))
    }
}
