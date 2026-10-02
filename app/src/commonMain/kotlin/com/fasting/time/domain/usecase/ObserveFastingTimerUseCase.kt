package com.fasting.time.domain.usecase

import com.fasting.time.domain.model.FastingTimer
import com.fasting.time.domain.repository.FastingWindowRepository
import com.fasting.time.domain.repository.TimeZoneRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Emits the timer once a second, or null while no window has been chosen. Every reading is worked
 * out from the wall clock and the window alone, so it is right again after the app was closed or
 * the phone restarted.
 */
class ObserveFastingTimerUseCase(
    private val fastingWindowRepository: FastingWindowRepository,
    private val timeZoneRepository: TimeZoneRepository,
    private val clock: Clock,
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<FastingTimer?> =
        fastingWindowRepository.observe().flatMapLatest { window ->
            if (window == null) {
                flowOf(null)
            } else {
                flow {
                    while (true) {
                        val now = clock.now()
                        val timer = window.timerAt(now, timeZoneRepository.utcOffsetAt(now))
                        emit(timer)
                        // Wake when the next whole second is gone, so the seconds never skip.
                        val intoSecond = timer.remaining - timer.remaining.inWholeSeconds.seconds
                        delay(if (intoSecond > Duration.ZERO) intoSecond else 1.seconds)
                    }
                }
            }
        }
}
