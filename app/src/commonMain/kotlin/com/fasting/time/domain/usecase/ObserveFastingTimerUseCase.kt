package com.fasting.time.domain.usecase

import com.fasting.time.domain.model.FastingTimer
import com.fasting.time.domain.repository.FastingRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds

/**
 * Emits the timer once a second, or null while no phase has been started. It counts from the
 * saved start on the wall clock, so it is right again after the app was closed or the phone
 * restarted.
 */
class ObserveFastingTimerUseCase(
    private val fastingRepository: FastingRepository,
    private val clock: Clock,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<FastingTimer?> =
        fastingRepository.observe().flatMapLatest { current ->
            if (current == null) {
                flowOf(null)
            } else {
                flow {
                    while (true) {
                        val elapsed = current.elapsedAt(clock.now())
                        emit(FastingTimer(current.phase, elapsed))
                        // Wake at the next whole second of the phase, so the seconds never skip.
                        val intoSecond = elapsed - elapsed.inWholeSeconds.seconds
                        delay(1.seconds - intoSecond)
                    }
                }
            }
        }
}
