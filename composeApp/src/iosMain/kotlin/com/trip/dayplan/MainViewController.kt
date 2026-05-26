package com.trip.dayplan

import androidx.compose.ui.window.ComposeUIViewController
import com.trip.dayplan.data.DatabaseDriverFactory
import com.trip.dayplan.di.AppDependencies
import com.trip.dayplan.ui.App
import com.trip.dayplan.ui.ScheduleViewModel

fun MainViewController() = ComposeUIViewController {
    val factory = DatabaseDriverFactory()
    val deps = AppDependencies(factory)
    val viewModel = ScheduleViewModel(deps.repository)
    App(viewModel)
}
