package com.trip.dayplan.data

import com.trip.dayplan.domain.AppSettings
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import platform.Foundation.NSUserDefaults

actual class SettingsStore {
    private val defaults = NSUserDefaults.standardUserDefaults

    actual val settings: Flow<AppSettings> = callbackFlow {
        trySend(readSettings())
        // NSUserDefaults doesn't have a direct change listener we can easily use from KMP
        // so we emit once and rely on manual refresh
        awaitClose {}
    }

    actual suspend fun updateSettings(update: (AppSettings) -> AppSettings) {
        val current = readSettings()
        val new = update(current)
        defaults.setBool(new.darkTheme, forKey = "dark_theme")
        defaults.setInteger(new.startHour.toLong(), forKey = "start_hour")
        defaults.setInteger(new.endHour.toLong(), forKey = "end_hour")
        defaults.setInteger(new.defaultDuration.toLong(), forKey = "default_duration")
        defaults.setInteger(new.defaultReminder.toLong(), forKey = "default_reminder")
        if (new.lastViewedDate != null) {
            defaults.setObject(new.lastViewedDate, forKey = "last_viewed_date")
        } else {
            defaults.removeObjectForKey("last_viewed_date")
        }
        defaults.synchronize()
    }

    private fun readSettings() = AppSettings(
        darkTheme = defaults.boolForKey("dark_theme"),
        startHour = defaults.integerForKey("start_hour").toInt(),
        endHour = defaults.integerForKey("end_hour").toInt(),
        defaultDuration = defaults.integerForKey("default_duration").toInt(),
        defaultReminder = defaults.integerForKey("default_reminder").toInt(),
        lastViewedDate = defaults.stringForKey("last_viewed_date"),
    )
}
