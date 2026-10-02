package com.fasting.time

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val container get() = (application as FastingTimeApplication).container

    private val askToNotify =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) lifecycleScope.launch { container.showPhaseNotification() }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        // The app is always dark, so keep the system bar icons light in both system themes.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        setContent {
            App(container)
        }
        // From Android 13 the phase notification needs the user's leave.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            askToNotify.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
