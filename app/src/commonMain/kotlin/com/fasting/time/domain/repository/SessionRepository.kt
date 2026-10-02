package com.fasting.time.domain.repository

import com.fasting.time.domain.model.Session
import kotlinx.coroutines.flow.Flow

interface SessionRepository {
    /** Emits every finished phase, oldest first. The log outlives the app. */
    fun observe(): Flow<List<Session>>

    suspend fun add(session: Session)
}
