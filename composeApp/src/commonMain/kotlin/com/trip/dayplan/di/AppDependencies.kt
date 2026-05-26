package com.trip.dayplan.di

import com.trip.dayplan.data.DatabaseDriverFactory
import com.trip.dayplan.data.DayPlanRepository
import com.trip.dayplan.data.SettingsStore
import com.trip.dayplan.db.DayPlanDatabase

/**
 * Application-level DI container. Created once at app startup per platform.
 */
class AppDependencies(
    databaseDriverFactory: DatabaseDriverFactory,
    settingsStoreFactory: () -> SettingsStore,
) {
    val driver = databaseDriverFactory.createDriver()
    val db = DayPlanDatabase(driver)
    val repository = DayPlanRepository(db)
    val settingsStore = settingsStoreFactory()
}
