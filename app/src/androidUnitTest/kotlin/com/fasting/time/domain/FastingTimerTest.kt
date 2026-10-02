package com.fasting.time.domain

import com.fasting.time.data.phase.SavedPhase
import com.fasting.time.data.repository.StoredFastingRepository
import com.fasting.time.data.repository.StoredSessionRepository
import com.fasting.time.domain.model.FastingPhase
import com.fasting.time.domain.model.FastingTimer
import com.fasting.time.domain.usecase.ObserveFastingTimerUseCase
import com.fasting.time.domain.usecase.StartPhaseUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

class FastingTimerTest {
    @Test
    fun timer_isEmptyUntilAPhaseIsStarted() = runBlocking {
        val repository = StoredFastingRepository(FakePhaseStore())

        assertNull(ObserveFastingTimerUseCase(repository, FakeClock()).invoke().first())
    }

    @Test
    fun timer_countsFromTheTap() = runBlocking {
        val clock = FakeClock()
        val store = FakePhaseStore()
        val repository = StoredFastingRepository(store)

        startPhaseUseCase(repository, clock)(FastingPhase.Fasting)
        clock.now += 16.hours

        assertEquals(
            FastingTimer(FastingPhase.Fasting, 16.hours),
            ObserveFastingTimerUseCase(repository, clock).invoke().first(),
        )
        assertEquals(SavedPhase("Fasting", Start.toEpochMilliseconds()), store.read())
    }

    @Test
    fun timer_carriesOnFromTheSavedPhase() = runBlocking {
        val clock = FakeClock(Start + 90.minutes)
        // A new repository over an old store is what a fresh launch of the app sees.
        val repository = StoredFastingRepository(
            FakePhaseStore(SavedPhase("Eating", Start.toEpochMilliseconds())),
        )

        assertEquals(
            FastingTimer(FastingPhase.Eating, 90.minutes),
            ObserveFastingTimerUseCase(repository, clock).invoke().first(),
        )
    }

    @Test
    fun timer_restartsWhenTheOtherPhaseBegins() = runBlocking {
        val clock = FakeClock()
        val repository = StoredFastingRepository(FakePhaseStore())
        val startPhase = startPhaseUseCase(repository, clock)

        startPhase(FastingPhase.Fasting)
        clock.now += 16.hours
        startPhase(FastingPhase.Eating)

        assertEquals(
            FastingTimer(FastingPhase.Eating, Duration.ZERO),
            ObserveFastingTimerUseCase(repository, clock).invoke().first(),
        )
    }

    @Test
    fun timer_neverRunsBackwards() = runBlocking {
        // The phone's clock was set back to before the phase began.
        val clock = FakeClock(Start - 5.minutes)
        val repository = StoredFastingRepository(
            FakePhaseStore(SavedPhase("Fasting", Start.toEpochMilliseconds())),
        )

        assertEquals(
            FastingTimer(FastingPhase.Fasting, Duration.ZERO),
            ObserveFastingTimerUseCase(repository, clock).invoke().first(),
        )
    }

    private fun startPhaseUseCase(repository: StoredFastingRepository, clock: FakeClock) =
        StartPhaseUseCase(repository, StoredSessionRepository(FakeSessionLogStore()), clock)
}
