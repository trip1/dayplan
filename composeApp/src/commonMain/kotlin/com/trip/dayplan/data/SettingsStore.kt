package com.trip.dayplan.data

import com.trip.dayplan.domain.AppSettings
import kotlinx.coroutines.flow.Flow

/**
 * Cross-platform settings persistence using expect/actual.
 * Wraps SharedPreferences (Android), NSUserDefaults (iOS), and a JSON file (Desktop).
 */
expect class SettingsStore {
    val settings: Flow<AppSettings>
    suspend fun updateSettings(update: (AppSettings) -> AppSettings)
}
