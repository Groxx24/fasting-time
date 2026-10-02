package com.fasting.time

import androidx.compose.ui.window.ComposeUIViewController
import com.fasting.time.data.repository.DeviceTimeZoneRepository
import com.fasting.time.data.window.UserDefaultsFastingWindowStore
import com.fasting.time.di.AppContainer
import com.fasting.time.ui.notification.LockScreenPhaseNotifier
import com.fasting.time.ui.notification.PhaseLiveActivity
import platform.UIKit.UIViewController

// Lives as long as the process, so it outlives any single view controller.
private var container: AppContainer? = null

/** Entry point called from the Swift side of the iOS app, which brings the Live Activity. */
fun MainViewController(liveActivity: PhaseLiveActivity): UIViewController {
    val container = container ?: AppContainer(
        fastingWindowStore = UserDefaultsFastingWindowStore(),
        phaseNotifier = LockScreenPhaseNotifier(liveActivity),
        timeZoneRepository = DeviceTimeZoneRepository(),
    ).also { container = it }
    return ComposeUIViewController { App(container) }
}
