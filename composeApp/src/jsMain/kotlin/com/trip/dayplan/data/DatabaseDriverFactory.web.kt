package com.trip.dayplan.data

import app.cash.sqldelight.db.SqlDriver

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        error("SQLDelight is not used by the static browser build. Use WebDayPlanStore instead.")
    }
}
