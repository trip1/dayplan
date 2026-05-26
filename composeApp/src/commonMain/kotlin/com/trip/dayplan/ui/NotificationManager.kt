package com.trip.dayplan.ui

import com.trip.dayplan.domain.DayTask

/**
 * Platform-agnostic notification interface.
 * Implemented per-platform for Android / iOS / Desktop.
 */
expect object NotificationManager {
    fun scheduleTaskReminder(taskName: String, minutesBefore: Int, timestampMillis: Long)
    fun scheduleNextTaskNotification(taskName: String, timestampMillis: Long)
    fun cancelAllNotifications()
    fun cancelNotification(id: Int)
}

/**
 * Check if a task is about to end (within 5 minutes).
 */
fun getTasksExpiringSoon(tasks: List<DayTask>, currentMinute: Int): List<DayTask> {
    return tasks.filter { task ->
        !task.isCompleted &&
            currentMinute >= (task.startMinute + task.durationMinutes - 5) &&
            currentMinute < (task.startMinute + task.durationMinutes)
    }
}
