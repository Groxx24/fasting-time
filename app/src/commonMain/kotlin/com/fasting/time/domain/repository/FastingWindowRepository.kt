package com.fasting.time.domain.repository

import com.fasting.time.domain.model.FastingWindow
import kotlinx.coroutines.flow.Flow

interface FastingWindowRepository {
    /** Emits the window the user chose, or null until they choose one. It outlives the app. */
    fun observe(): Flow<FastingWindow?>

    suspend fun save(window: FastingWindow)
}
