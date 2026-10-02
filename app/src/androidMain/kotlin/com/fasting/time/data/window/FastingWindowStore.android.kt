package com.fasting.time.data.window

import android.content.Context

class SharedPreferencesFastingWindowStore(context: Context) : FastingWindowStore {
    private val preferences = context.getSharedPreferences("fasting_window", Context.MODE_PRIVATE)

    override fun read(): SavedWindow? {
        if (!preferences.contains(StartKey)) return null
        return SavedWindow(preferences.getInt(StartKey, 0), preferences.getInt(EndKey, 0))
    }

    override fun write(window: SavedWindow) {
        preferences.edit()
            .putInt(StartKey, window.startMinuteOfDay)
            .putInt(EndKey, window.endMinuteOfDay)
            .apply()
    }

    private companion object {
        const val StartKey = "start"
        const val EndKey = "end"
    }
}
