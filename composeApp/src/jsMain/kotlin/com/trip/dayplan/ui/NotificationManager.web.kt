package com.trip.dayplan.ui

actual object NotificationManager {
    actual fun scheduleTaskReminder(taskName: String, minutesBefore: Int, timestampMillis: Long) = Unit
    actual fun scheduleNextTaskNotification(taskName: String, timestampMillis: Long) = Unit
    actual fun cancelAllNotifications() = Unit
    actual fun cancelNotification(id: Int) = Unit
}
