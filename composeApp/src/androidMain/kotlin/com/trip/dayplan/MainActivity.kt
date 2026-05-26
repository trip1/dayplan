package com.trip.dayplan

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext
import com.trip.dayplan.data.DatabaseDriverFactory
import com.trip.dayplan.di.AppDependencies
import com.trip.dayplan.ui.App
import com.trip.dayplan.ui.ScheduleViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val factory = DatabaseDriverFactory(this)
        val deps = AppDependencies(factory)
        val viewModel = ScheduleViewModel(deps.repository)

        setContent {
            App(viewModel)
        }
    }
}
