package com.fasting.time.data.window

/** Where the chosen fasting window is kept between launches. Each platform has its own. */
interface FastingWindowStore {
    /** The saved window, or null if none has been chosen yet. */
    fun read(): SavedWindow?

    fun write(window: SavedWindow)
}

/** A window as it is stored: the minutes since midnight at which the fast starts and ends. */
data class SavedWindow(val startMinuteOfDay: Int, val endMinuteOfDay: Int)
