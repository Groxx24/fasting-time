package com.fasting.time.data.repository

import com.fasting.time.data.window.FastingWindowStore
import com.fasting.time.data.window.SavedWindow
import com.fasting.time.domain.model.FastingWindow
import com.fasting.time.domain.model.TimeOfDay
import com.fasting.time.domain.repository.FastingWindowRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class StoredFastingWindowRepository(private val store: FastingWindowStore) : FastingWindowRepository {
    // Read once, on first use; after that the store is only written to.
    private val window by lazy { MutableStateFlow(store.read()?.toFastingWindow()) }

    override fun observe(): Flow<FastingWindow?> = window

    override suspend fun save(window: FastingWindow) {
        this.window.value = window
        store.write(SavedWindow(window.start.minuteOfDay, window.end.minuteOfDay))
    }

    /** Null for times that are not on the clock, which is treated as nothing saved. */
    private fun SavedWindow.toFastingWindow(): FastingWindow? {
        val minutesOfDay = 0 until MinutesPerDay
        if (startMinuteOfDay !in minutesOfDay || endMinuteOfDay !in minutesOfDay) return null
        return FastingWindow(TimeOfDay(startMinuteOfDay), TimeOfDay(endMinuteOfDay))
    }

    private companion object {
        const val MinutesPerDay = 24 * 60
    }
}
