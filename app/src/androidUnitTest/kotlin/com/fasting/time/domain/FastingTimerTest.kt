package com.fasting.time.domain

import com.fasting.time.data.repository.StoredFastingWindowRepository
import com.fasting.time.data.window.SavedWindow
import com.fasting.time.domain.model.FastingPhase
import com.fasting.time.domain.model.FastingTimer
import com.fasting.time.domain.model.FastingWindow
import com.fasting.time.domain.model.TimeOfDay
import com.fasting.time.domain.usecase.ObserveFastingTimerUseCase
import com.fasting.time.domain.usecase.SetFastingWindowUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class FastingTimerTest {
    /** From eight in the evening until noon, which is 1200 and 720 minutes into the day. */
    private val overnight = FastingWindow(TimeOfDay(20, 0), TimeOfDay(12, 0))
    private val savedOvernight = SavedWindow(startMinuteOfDay = 1200, endMinuteOfDay = 720)

    @Test
    fun timer_isEmptyUntilAWindowIsChosen() = runBlocking {
        assertNull(timerAt(FakeClock(), FakeFastingWindowStore()))
    }

    @Test
    fun timer_countsDownToTheFastWhileEating() = runBlocking {
        assertEquals(
            FastingTimer(FastingPhase.Eating, 30.minutes, overnight),
            timerAt(FakeClock(), FakeFastingWindowStore(savedOvernight)),
        )
    }

    @Test
    fun timer_countsDownToEatingWhileFasting() = runBlocking {
        val clock = FakeClock(Start + 45.minutes + 10.seconds)

        assertEquals(
            FastingTimer(FastingPhase.Fasting, 16.hours - 15.minutes - 10.seconds, overnight),
            timerAt(clock, FakeFastingWindowStore(savedOvernight)),
        )
    }

    @Test
    fun timer_keepsFastingPastMidnight() = runBlocking {
        // Three in the morning on the next day.
        val clock = FakeClock(Start + 7.hours + 30.minutes)

        assertEquals(
            FastingTimer(FastingPhase.Fasting, 9.hours, overnight),
            timerAt(clock, FakeFastingWindowStore(savedOvernight)),
        )
    }

    @Test
    fun timer_changesPhaseOnTheMinuteTheWindowNames() = runBlocking {
        val store = FakeFastingWindowStore(savedOvernight)

        assertEquals(
            FastingTimer(FastingPhase.Fasting, 16.hours, overnight),
            timerAt(FakeClock(Start + 30.minutes), store),
        )
        assertEquals(
            FastingTimer(FastingPhase.Eating, 8.hours, overnight),
            timerAt(FakeClock(Start + 16.hours + 30.minutes), store),
        )
    }

    @Test
    fun timer_goesByLocalTime() = runBlocking {
        // Half past seven in UTC is half past nine two hours east of it, and half past two
        // in the afternoon five hours west.
        val store = FakeFastingWindowStore(savedOvernight)

        assertEquals(
            FastingTimer(FastingPhase.Fasting, 14.hours + 30.minutes, overnight),
            timerAt(FakeClock(), store, utcOffset = 2.hours),
        )
        assertEquals(
            FastingTimer(FastingPhase.Eating, 5.hours + 30.minutes, overnight),
            timerAt(FakeClock(), store, utcOffset = (-5).hours),
        )
    }

    @Test
    fun timer_followsAWindowWithinOneDay() = runBlocking {
        val daytime = FastingWindow(TimeOfDay(8, 0), TimeOfDay(18, 0))
        val store = FakeFastingWindowStore(SavedWindow(startMinuteOfDay = 480, endMinuteOfDay = 1080))

        assertEquals(
            FastingTimer(FastingPhase.Eating, 12.hours + 30.minutes, daytime),
            timerAt(FakeClock(), store),
        )
        assertEquals(
            FastingTimer(FastingPhase.Fasting, 1.hours, daytime),
            timerAt(FakeClock(Start - 2.hours - 30.minutes), store),
        )
    }

    @Test
    fun timer_followsANewlyChosenWindow() = runBlocking {
        val store = FakeFastingWindowStore(savedOvernight)
        val repository = StoredFastingWindowRepository(store)
        val evening = FastingWindow(TimeOfDay(19, 0), TimeOfDay(7, 15))

        SetFastingWindowUseCase(repository)(evening)

        assertEquals(
            FastingTimer(FastingPhase.Fasting, 11.hours + 45.minutes, evening),
            ObserveFastingTimerUseCase(repository, FixedTimeZoneRepository(), FakeClock())
                .invoke().first(),
        )
        assertEquals(SavedWindow(startMinuteOfDay = 1140, endMinuteOfDay = 435), store.read())
    }

    @Test
    fun timer_ignoresASavedWindowThatIsNotOnTheClock() = runBlocking {
        val store = FakeFastingWindowStore(SavedWindow(startMinuteOfDay = 1200, endMinuteOfDay = 1440))

        assertNull(timerAt(FakeClock(), store))
    }

    // A new repository over a store is what a fresh launch of the app sees.
    private suspend fun timerAt(
        clock: FakeClock,
        store: FakeFastingWindowStore,
        utcOffset: Duration = Duration.ZERO,
    ): FastingTimer? =
        ObserveFastingTimerUseCase(
            StoredFastingWindowRepository(store),
            FixedTimeZoneRepository(utcOffset),
            clock,
        ).invoke().first()
}
