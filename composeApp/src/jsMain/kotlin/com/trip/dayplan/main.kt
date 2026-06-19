package com.trip.dayplan

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import com.trip.dayplan.data.SettingsStore
import com.trip.dayplan.data.WebDayPlanStore
import com.trip.dayplan.ui.App
import com.trip.dayplan.ui.ScheduleViewModel

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val repository = WebDayPlanStore()
    val settingsStore = SettingsStore()
    val viewModel = ScheduleViewModel(repository, settingsStore)

    CanvasBasedWindow("DayPlan") {
        App(viewModel)
    }
}
