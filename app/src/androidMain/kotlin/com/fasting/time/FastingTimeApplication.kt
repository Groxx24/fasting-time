package com.fasting.time

import android.app.Application
import com.fasting.time.data.repository.DeviceTimeZoneRepository
import com.fasting.time.data.window.SharedPreferencesFastingWindowStore
import com.fasting.time.di.AppContainer

class FastingTimeApplication : Application() {
    /** Lives as long as the process, so it outlives any single activity. */
    val container: AppContainer by lazy {
        AppContainer(
            fastingWindowStore = SharedPreferencesFastingWindowStore(this),
            timeZoneRepository = DeviceTimeZoneRepository(),
        )
    }
}
