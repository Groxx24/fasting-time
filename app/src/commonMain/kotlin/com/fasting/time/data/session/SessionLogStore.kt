package com.fasting.time.data.session

/** Where the log of finished phases is kept between launches, as text. Each platform has its own. */
interface SessionLogStore {
    /** The saved log, or null if nothing has been logged yet. */
    fun read(): String?

    fun write(log: String)
}
