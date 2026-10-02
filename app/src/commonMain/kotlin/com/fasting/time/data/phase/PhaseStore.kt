package com.fasting.time.data.phase

/** Where the phase under way is kept between launches. Each platform has its own. */
interface PhaseStore {
    /** The saved phase, or null if none has been started yet. */
    fun read(): SavedPhase?

    fun write(phase: SavedPhase)
}

/** A phase as it is stored: its name and the moment it began, in milliseconds since the epoch. */
data class SavedPhase(val name: String, val startedAtEpochMillis: Long)
