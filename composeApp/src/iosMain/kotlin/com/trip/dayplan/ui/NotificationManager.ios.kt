package com.trip.dayplan.ui

import platform.Foundation.*
import platform.UserNotifications.*

actual object NotificationManager {
    private var center: UNUserNotificationCenter? = null

    fun initialize() {
        center = UNUserNotificationCenter.currentNotificationCenter()
        center?.requestAuthorizationWithOptions(
            UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge,
        ) { granted, error ->
            if (error != null) {
                println("Notification permission denied: ${error.localizedDescription}")
            }
        }
    }

    actual fun scheduleTaskReminder(taskName: String, minutesBefore: Int, timestampMillis: Long) {
        val center = center ?: return
        val date = NSDate.dateWithTimeIntervalSince1970(timestampMillis / 1000.0 - minutesBefore * 60)
        val components = NSCalendar.currentCalendar.componentsInTimeZone(
            NSTimeZone.localTimeZone(), fromDate = date
        )
        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
            components, repeats = false
        )
        val content = UNMutableNotificationContent().apply {
            setTitle("Task ending soon")
            setBody("$taskName ends in $minutesBefore minutes")
            setSound(UNNotificationSound.defaultSound())
        }
        val request = UNNotificationRequest.requestWithIdentifier(
            "reminder_${timestampMillis}", content, trigger
        )
        center.addNotificationRequest(request) { error ->
            if (error != null) println("Failed to schedule reminder: ${error.localizedDescription}")
        }
    }

    actual fun scheduleNextTaskNotification(taskName: String, timestampMillis: Long) {
        val center = center ?: return
        val date = NSDate.dateWithTimeIntervalSince1970(timestampMillis / 1000.0)
        val components = NSCalendar.currentCalendar.componentsInTimeZone(
            NSTimeZone.localTimeZone(), fromDate = date
        )
        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
            components, repeats = false
        )
        val content = UNMutableNotificationContent().apply {
            setTitle("Next task")
            setBody("$taskName is starting now")
            setSound(UNNotificationSound.defaultSound())
        }
        val request = UNNotificationRequest.requestWithIdentifier(
            "next_task_${timestampMillis}", content, trigger
        )
        center.addNotificationRequest(request) { error ->
            if (error != null) println("Failed to schedule next task notification: ${error.localizedDescription}")
        }
    }

    actual fun cancelAllNotifications() {
        center?.removeAllPendingNotificationRequests()
    }

    actual fun cancelNotification(id: Int) {
        center?.removePendingNotificationRequestsWithIdentifiers(listOf(id.toString()))
    }
}
