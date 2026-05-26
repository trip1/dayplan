package com.trip.dayplan.ui

actual object NotificationManager {
    actual fun scheduleTaskReminder(taskName: String, minutesBefore: Int, timestampMillis: Long) {
        // Desktop doesn't have native scheduling — log for now
        println("[DayPlan] Task reminder: $taskName ends in $minutesBefore minutes")
    }

    actual fun scheduleNextTaskNotification(taskName: String, timestampMillis: Long) {
        println("[DayPlan] Next task: $taskName is starting now")
    }

    actual fun cancelAllNotifications() {
        // No-op for desktop
    }

    actual fun cancelNotification(id: Int) {
        // No-op for desktop
    }
}
