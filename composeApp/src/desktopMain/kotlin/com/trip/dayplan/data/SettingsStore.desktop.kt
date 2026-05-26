package com.trip.dayplan.data

import com.trip.dayplan.domain.AppSettings
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import java.io.File
import java.util.Properties

actual class SettingsStore {
    private val file = File(System.getProperty("user.home"), ".dayplan/settings.properties")

    init {
        file.parentFile?.mkdirs()
    }

    actual val settings: Flow<AppSettings> = callbackFlow {
        trySend(readSettings())
        awaitClose {}
    }

    actual suspend fun updateSettings(update: (AppSettings) -> AppSettings) {
        val current = readSettings()
        val new = update(current)
        val props = Properties()
        props.setProperty("dark_theme", new.darkTheme.toString())
        props.setProperty("start_hour", new.startHour.toString())
        props.setProperty("end_hour", new.endHour.toString())
        props.setProperty("default_duration", new.defaultDuration.toString())
        props.setProperty("default_reminder", new.defaultReminder.toString())
        if (new.lastViewedDate != null) {
            props.setProperty("last_viewed_date", new.lastViewedDate)
        }
        file.outputStream().use { props.store(it, "DayPlan Settings") }
    }

    private fun readSettings(): AppSettings {
        if (!file.exists()) return AppSettings()
        val props = Properties()
        file.inputStream().use { props.load(it) }
        return AppSettings(
            darkTheme = props.getProperty("dark_theme", "false").toBoolean(),
            startHour = props.getProperty("start_hour", "6").toInt(),
            endHour = props.getProperty("end_hour", "23").toInt(),
            defaultDuration = props.getProperty("default_duration", "30").toInt(),
            defaultReminder = props.getProperty("default_reminder", "5").toInt(),
            lastViewedDate = props.getProperty("last_viewed_date"),
        )
    }
}
