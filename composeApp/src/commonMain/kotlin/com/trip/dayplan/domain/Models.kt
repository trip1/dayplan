package com.trip.dayplan.domain

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.serialization.Serializable

/**
 * A task that occupies a time slot in the daily schedule.
 * Duration is stored in minutes; startMinute is minutes since midnight.
 */
@Serializable
data class DayTask(
    val id: Long = 0,
    val name: String = "",
    val description: String = "",
    val durationMinutes: Int = 30,
    val groupId: Long? = null,
    val date: String = "",       // ISO date string (yyyy-MM-dd)
    val startMinute: Int = 0,    // minutes since midnight (0-1435)
    val isCompleted: Boolean = false,
    val reminderMinutes: Int = 5, // minutes before task end to remind (0 = disabled)
    val groupName: String? = null,
    val groupColorHex: String? = null,
) {
    val startTime: LocalTime
        get() = LocalTime(startMinute / 60, startMinute % 60)

    val endTime: LocalTime
        get() {
            val total = startMinute + durationMinutes
            return LocalTime((total / 60) % 24, total % 60)
        }

    val colorHex: String
        get() = groupColorHex ?: "#999999"
}

/**
 * A group/category for organizing tasks.
 */
@Serializable
data class TaskGroup(
    val id: Long = 0,
    val name: String = "",
    val colorHex: String = "#4A6741",
    val sortOrder: Int = 0,
)

/**
 * App-wide user settings, persisted across launches.
 */
@Serializable
data class AppSettings(
    val darkTheme: Boolean = false,
    val startHour: Int = 6,       // Timeline starts at this hour
    val endHour: Int = 23,        // Timeline ends at this hour
    val defaultDuration: Int = 30, // Default task duration in minutes
    val defaultReminder: Int = 5,  // Default reminder minutes before end
    val lastViewedDate: String? = null,
)

/**
 * Represents the current day's schedule state.
 */
data class DaySchedule(
    val date: String = kotlinx.datetime.Clock.System.now()
        .toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
        .date.toString(),
    val tasks: List<DayTask> = emptyList(),
    val groups: List<TaskGroup> = emptyList(),
)

/**
 * Default task groups shipped with the app.
 */
object DefaultGroups {
    val groups = listOf(
        TaskGroup(name = "Home", colorHex = "#4A6741", sortOrder = 0),      // Sage green
        TaskGroup(name = "Family", colorHex = "#E8913A", sortOrder = 1),    // Warm orange
        TaskGroup(name = "Errands", colorHex = "#5B8DB8", sortOrder = 2),   // Steel blue
        TaskGroup(name = "Self-care", colorHex = "#8B5CF6", sortOrder = 3), // Purple
    )
}
