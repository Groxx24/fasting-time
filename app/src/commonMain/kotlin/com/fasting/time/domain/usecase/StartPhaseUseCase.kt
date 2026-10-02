package com.fasting.time.domain.usecase

import com.fasting.time.domain.model.CurrentPhase
import com.fasting.time.domain.model.FastingPhase
import com.fasting.time.domain.model.Session
import com.fasting.time.domain.repository.FastingRepository
import com.fasting.time.domain.repository.SessionRepository
import kotlinx.coroutines.flow.first
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

/**
 * Starts a phase now, which restarts the timer, ends the phase that was under way and puts that
 * one in the log.
 */
class StartPhaseUseCase(
    private val fastingRepository: FastingRepository,
    private val sessionRepository: SessionRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(phase: FastingPhase) {
        val now = clock.now()
        fastingRepository.observe().first()?.let { ended ->
            val session = Session(ended.phase, ended.startedAt, now)
            if (session.duration >= ShortestLogged) sessionRepository.add(session)
        }
        fastingRepository.save(CurrentPhase(phase, now))
    }

    private companion object {
        /** Anything shorter was a tap by mistake, not a fast or a meal. */
        val ShortestLogged = 1.minutes
    }
}
