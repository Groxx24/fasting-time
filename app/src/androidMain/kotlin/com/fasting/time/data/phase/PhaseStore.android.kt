package com.fasting.time.data.phase

import android.content.Context

class SharedPreferencesPhaseStore(context: Context) : PhaseStore {
    private val preferences = context.getSharedPreferences("current_phase", Context.MODE_PRIVATE)

    override fun read(): SavedPhase? {
        val name = preferences.getString(NameKey, null) ?: return null
        return SavedPhase(name, preferences.getLong(StartedAtKey, 0))
    }

    override fun write(phase: SavedPhase) {
        preferences.edit()
            .putString(NameKey, phase.name)
            .putLong(StartedAtKey, phase.startedAtEpochMillis)
            .apply()
    }

    private companion object {
        const val NameKey = "name"
        const val StartedAtKey = "started_at"
    }
}
