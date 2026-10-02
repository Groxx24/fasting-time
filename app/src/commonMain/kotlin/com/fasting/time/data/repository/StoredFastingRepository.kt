package com.fasting.time.data.repository

import com.fasting.time.data.phase.PhaseStore
import com.fasting.time.data.phase.SavedPhase
import com.fasting.time.domain.model.CurrentPhase
import com.fasting.time.domain.model.FastingPhase
import com.fasting.time.domain.repository.FastingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.time.Instant

class StoredFastingRepository(private val store: PhaseStore) : FastingRepository {
    // Read once, on first use; after that the store is only written to.
    private val current by lazy { MutableStateFlow(store.read()?.toCurrentPhase()) }

    override fun observe(): Flow<CurrentPhase?> = current

    override suspend fun save(current: CurrentPhase) {
        this.current.value = current
        store.write(SavedPhase(current.phase.name, current.startedAt.toEpochMilliseconds()))
    }

    /** Null for a name this version doesn't know, which is treated as nothing saved. */
    private fun SavedPhase.toCurrentPhase(): CurrentPhase? {
        val phase = FastingPhase.entries.firstOrNull { it.name == name } ?: return null
        return CurrentPhase(phase, Instant.fromEpochMilliseconds(startedAtEpochMillis))
    }
}
