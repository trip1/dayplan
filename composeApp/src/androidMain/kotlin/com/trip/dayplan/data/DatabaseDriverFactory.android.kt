package com.trip.dayplan.data

import android.content.Context
import android.content.SharedPreferences
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.trip.dayplan.db.DayPlanDatabase

actual class DatabaseDriverFactory(
    private val context: Context,
) {
    actual fun createDriver(): SqlDriver {
        val driver = AndroidSqliteDriver(DayPlanDatabase.Schema, context, "dayplan.db")
        migrateIfNeeded(driver, context)
        return driver
    }

    private fun migrateIfNeeded(driver: SqlDriver, context: Context) {
        val prefs: SharedPreferences = context.getSharedPreferences("dayplan_schema_version", Context.MODE_PRIVATE)
        val currentVersion = 1
        val savedVersion = prefs.getInt("schema_version", 0)
        if (savedVersion >= currentVersion) return

        prefs.edit().putInt("schema_version", currentVersion).apply()
    }
}
