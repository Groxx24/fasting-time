package com.fasting.time.domain

import com.fasting.time.data.repository.StoredFastingRepository
import com.fasting.time.data.repository.StoredSessionRepository
import com.fasting.time.domain.model.FastingPhase
import com.fasting.time.domain.model.Session
import com.fasting.time.domain.usecase.StartPhaseUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

class SessionLogTest {
    private val clock = FakeClock()
    private val store = FakeSessionLogStore()
    private val sessions = StoredSessionRepository(store)
    private val startPhase =
        StartPhaseUseCase(StoredFastingRepository(FakePhaseStore()), sessions, clock)

    @Test
    fun startingAPhase_logsTheOneItEnds() = runBlocking {
        startPhase(FastingPhase.Fasting)
        clock.now += 16.hours
        startPhase(FastingPhase.Eating)
        clock.now += 8.hours
        startPhase(FastingPhase.Fasting)

        assertEquals(
            listOf(
                Session(FastingPhase.Fasting, Start, Start + 16.hours),
                Session(FastingPhase.Eating, Start + 16.hours, Start + 24.hours),
            ),
            sessions.observe().first(),
        )
    }

    @Test
    fun startingTheFirstPhase_logsNothing() = runBlocking {
        startPhase(FastingPhase.Fasting)

        assertEquals(emptyList<Session>(), sessions.observe().first())
    }

    @Test
    fun aTapByMistake_isNotLogged() = runBlocking {
        startPhase(FastingPhase.Fasting)
        clock.now += 16.hours
        startPhase(FastingPhase.Eating)
        clock.now += 20.seconds
        startPhase(FastingPhase.Fasting)

        assertEquals(
            listOf(Session(FastingPhase.Fasting, Start, Start + 16.hours)),
            sessions.observe().first(),
        )
    }

    @Test
    fun theLog_isReadBackOnTheNextLaunch() = runBlocking {
        startPhase(FastingPhase.Fasting)
        clock.now += 16.hours
        startPhase(FastingPhase.Eating)
        clock.now += 8.hours
        startPhase(FastingPhase.Fasting)

        // A new repository over the same store is what a fresh launch of the app sees.
        assertEquals(
            sessions.observe().first(),
            StoredSessionRepository(FakeSessionLogStore(store.log)).observe().first(),
        )
    }

    @Test
    fun theLog_skipsWhatItCannotRead() = runBlocking {
        val start = Start.toEpochMilliseconds()
        val log = "Fasting,$start,${start + 1000};Napping,1,2;Eating,soon,later;;Eating,$start"

        assertEquals(
            listOf(Session(FastingPhase.Fasting, Start, Start + 1.seconds)),
            StoredSessionRepository(FakeSessionLogStore(log)).observe().first(),
        )
    }
}
