package com.trip.dayplan

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.trip.dayplan.data.DatabaseDriverFactory
import com.trip.dayplan.data.SettingsStore
import com.trip.dayplan.di.AppDependencies
import com.trip.dayplan.ui.App
import com.trip.dayplan.ui.NotificationManager
import com.trip.dayplan.ui.ScheduleViewModel

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission(),
    ) { _ ->
        // Permission result — notification manager handles it
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize notification manager with app context
        NotificationManager.initialize(applicationContext)

        // Request notification permission on Android 13+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS,
                ) != android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        val factory = DatabaseDriverFactory(this)
        val deps = AppDependencies(factory) { SettingsStore(this) }
        val viewModel = ScheduleViewModel(deps.repository, deps.settingsStore)

        setContent {
            App(viewModel)
        }
    }
}
