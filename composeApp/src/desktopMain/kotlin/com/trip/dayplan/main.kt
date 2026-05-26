package com.trip.dayplan

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.trip.dayplan.data.DatabaseDriverFactory
import com.trip.dayplan.di.AppDependencies
import com.trip.dayplan.ui.App
import com.trip.dayplan.ui.ScheduleViewModel

fun main() = application {
    val factory = DatabaseDriverFactory()
    val deps = AppDependencies(factory)
    val viewModel = ScheduleViewModel(deps.repository)

    Window(
        onCloseRequest = ::exitApplication,
        title = "DayPlan",
    ) {
        App(viewModel)
    }
}
