package com.fasting.time

import androidx.compose.runtime.Composable
import com.fasting.time.di.AppContainer
import com.fasting.time.ui.main.MainRoute
import com.fasting.time.ui.theme.FastingTimeTheme

/** The whole app, shared by every platform's entry point. */
@Composable
fun App(container: AppContainer) {
    FastingTimeTheme {
        MainRoute(container)
    }
}
