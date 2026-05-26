package com.trip.dayplan.data

import android.content.Context
import android.content.SharedPreferences
import com.trip.dayplan.domain.AppSettings
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

actual class SettingsStore(private val context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("dayplan_settings", Context.MODE_PRIVATE)

    actual val settings: Flow<AppSettings> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
            trySend(readSettings())
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(readSettings())
        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    actual suspend fun updateSettings(update: (AppSettings) -> AppSettings) {
        val current = readSettings()
        val new = update(current)
        with(prefs.edit()) {
            putBoolean("dark_theme", new.darkTheme)
            putInt("start_hour", new.startHour)
            putInt("end_hour", new.endHour)
            putInt("default_duration", new.defaultDuration)
            putInt("default_reminder", new.defaultReminder)
            putString("last_viewed_date", new.lastViewedDate)
            apply()
        }
    }

    private fun readSettings() = AppSettings(
        darkTheme = prefs.getBoolean("dark_theme", false),
        startHour = prefs.getInt("start_hour", 6),
        endHour = prefs.getInt("end_hour", 23),
        defaultDuration = prefs.getInt("default_duration", 30),
        defaultReminder = prefs.getInt("default_reminder", 5),
        lastViewedDate = prefs.getString("last_viewed_date", null),
    )
}
