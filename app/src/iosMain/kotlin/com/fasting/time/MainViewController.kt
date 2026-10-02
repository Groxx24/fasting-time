package com.fasting.time

import androidx.compose.ui.window.ComposeUIViewController
import com.fasting.time.data.phase.UserDefaultsPhaseStore
import com.fasting.time.data.repository.DeviceTimeZoneRepository
import com.fasting.time.data.session.UserDefaultsSessionLogStore
import com.fasting.time.di.AppContainer
import platform.UIKit.UIViewController

// Lives as long as the process, so it outlives any single view controller.
private val container by lazy {
    AppContainer(
        phaseStore = UserDefaultsPhaseStore(),
        sessionLogStore = UserDefaultsSessionLogStore(),
        timeZoneRepository = DeviceTimeZoneRepository(),
    )
}

/** Entry point called from the Swift side of the iOS app. */
fun MainViewController(): UIViewController = ComposeUIViewController { App(container) }
