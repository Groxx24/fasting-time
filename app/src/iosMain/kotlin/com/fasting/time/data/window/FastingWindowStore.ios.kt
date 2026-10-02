package com.fasting.time.data.window

import platform.Foundation.NSUserDefaults

class UserDefaultsFastingWindowStore : FastingWindowStore {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun read(): SavedWindow? {
        if (defaults.objectForKey(StartKey) == null) return null
        return SavedWindow(
            defaults.integerForKey(StartKey).toInt(),
            defaults.integerForKey(EndKey).toInt(),
        )
    }

    override fun write(window: SavedWindow) {
        defaults.setInteger(window.startMinuteOfDay.toLong(), forKey = StartKey)
        defaults.setInteger(window.endMinuteOfDay.toLong(), forKey = EndKey)
    }

    private companion object {
        const val StartKey = "fasting_window_start"
        const val EndKey = "fasting_window_end"
    }
}
