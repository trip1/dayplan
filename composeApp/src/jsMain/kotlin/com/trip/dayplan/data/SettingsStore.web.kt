package com.trip.dayplan.data

import com.trip.dayplan.domain.AppSettings
import kotlinx.browser.window
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

actual class SettingsStore {
    private val json = Json { ignoreUnknownKeys = true }
    private val state = MutableStateFlow(readSettings())

    actual val settings: Flow<AppSettings> = state

    actual suspend fun updateSettings(update: (AppSettings) -> AppSettings) {
        val updated = update(state.value)
        window.localStorage.setItem(KEY, json.encodeToString(updated))
        state.value = updated
    }

    private fun readSettings(): AppSettings {
        val raw = window.localStorage.getItem(KEY) ?: return AppSettings()
        return runCatching { json.decodeFromString<AppSettings>(raw) }.getOrDefault(AppSettings())
    }

    private companion object {
        const val KEY = "dayplan.settings"
    }
}
