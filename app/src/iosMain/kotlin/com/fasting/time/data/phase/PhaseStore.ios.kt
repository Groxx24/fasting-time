package com.fasting.time.data.phase

import platform.Foundation.NSUserDefaults

class UserDefaultsPhaseStore : PhaseStore {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun read(): SavedPhase? {
        val name = defaults.stringForKey(NameKey) ?: return null
        return SavedPhase(name, defaults.integerForKey(StartedAtKey))
    }

    override fun write(phase: SavedPhase) {
        defaults.setObject(phase.name, forKey = NameKey)
        defaults.setInteger(phase.startedAtEpochMillis, forKey = StartedAtKey)
    }

    private companion object {
        const val NameKey = "current_phase_name"
        const val StartedAtKey = "current_phase_started_at"
    }
}
