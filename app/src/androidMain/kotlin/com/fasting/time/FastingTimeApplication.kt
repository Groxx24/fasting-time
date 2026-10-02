package com.fasting.time

import android.app.Application
import com.fasting.time.data.phase.SharedPreferencesPhaseStore
import com.fasting.time.di.AppContainer

class FastingTimeApplication : Application() {
    /** Lives as long as the process, so it outlives any single activity. */
    val container: AppContainer by lazy { AppContainer(SharedPreferencesPhaseStore(this)) }
}
