package com.trip.dayplan.data

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.trip.dayplan.db.DayPlanDatabase
import platform.Foundation.NSUserDefaults

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        val driver = NativeSqliteDriver(DayPlanDatabase.Schema, "dayplan.db")
        migrateIfNeeded(driver)
        return driver
    }

    private fun migrateIfNeeded(driver: SqlDriver) {
        val prefs = NSUserDefaults.standardUserDefaults
        val currentVersion = 1
        val savedVersion = prefs.integerForKey("dayplan_schema_version").toInt()
        if (savedVersion >= currentVersion) return

        prefs.setInteger(currentVersion.toLong(), "dayplan_schema_version")
    }
}
