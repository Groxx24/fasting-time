package com.fasting.time.domain

import com.fasting.time.data.repository.StoredFastingWindowRepository
import com.fasting.time.data.window.SavedWindow
import com.fasting.time.domain.model.FastingPhase
import com.fasting.time.domain.model.FastingTimer
import com.fasting.time.domain.model.FastingWindow
import com.fasting.time.domain.model.TimeOfDay
import com.fasting.time.domain.usecase.ShowPhaseNotificationUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class PhaseNotificationTest {
    private val overnight = FastingWindow(TimeOfDay(20, 0), TimeOfDay(12, 0))
    private val savedOvernight = SavedWindow(startMinuteOfDay = 1200, endMinuteOfDay = 720)

    @Test
    fun notification_isNotShownUntilAWindowIsChosen() = runBlocking {
        val notifier = FakePhaseNotifier()

        show(notifier, FakeFastingWindowStore())

        assertEquals(emptyList<Pair<FastingTimer, Instant>>(), notifier.shown)
    }

    @Test
    fun notification_showsThePhaseAndTheMomentItEnds() = runBlocking {
        val notifier = FakePhaseNotifier()

        show(notifier, FakeFastingWindowStore(savedOvernight))

        assertEquals(
            listOf(
                FastingTimer(FastingPhase.Eating, 30.minutes, overnight) to
                    Instant.parse("2026-10-02T20:00:00Z"),
            ),
            notifier.shown,
        )
    }

    @Test
    fun notification_endsOnTheLocalClock() = runBlocking {
        val notifier = FakePhaseNotifier()

        // Half past nine in the evening two hours east of UTC, where noon is ten o'clock UTC.
        show(notifier, FakeFastingWindowStore(savedOvernight), utcOffset = 2.hours)

        assertEquals(
            listOf(
                FastingTimer(FastingPhase.Fasting, 14.hours + 30.minutes, overnight) to
                    Instant.parse("2026-10-03T10:00:00Z"),
            ),
            notifier.shown,
        )
    }

    private suspend fun show(
        notifier: FakePhaseNotifier,
        store: FakeFastingWindowStore,
        utcOffset: Duration = Duration.ZERO,
    ) = ShowPhaseNotificationUseCase(
        StoredFastingWindowRepository(store),
        FixedTimeZoneRepository(utcOffset),
        notifier,
        FakeClock(),
    ).invoke()
}
