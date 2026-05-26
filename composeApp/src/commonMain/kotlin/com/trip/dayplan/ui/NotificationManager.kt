package com.trip.dayplan.ui

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
