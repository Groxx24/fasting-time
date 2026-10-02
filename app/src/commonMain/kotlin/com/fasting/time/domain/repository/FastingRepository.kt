package com.fasting.time.domain.repository

import com.fasting.time.domain.model.CurrentPhase
import kotlinx.coroutines.flow.Flow

interface FastingRepository {
    /** Emits the phase under way, or null until the first one is started. It outlives the app. */
    fun observe(): Flow<CurrentPhase?>

    suspend fun save(current: CurrentPhase)
}
