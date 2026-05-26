package com.trip.dayplan.ui

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import com.trip.dayplan.NotificationReceiver
import java.util.Calendar

actual object NotificationManager {
    private var context: Context? = null

    fun initialize(ctx: Context) {
        context = ctx.applicationContext
    }

    actual fun scheduleTaskReminder(taskName: String, minutesBefore: Int, timestampMillis: Long) {
        val ctx = context ?: return
        val alarmTime = timestampMillis - (minutesBefore * 60_000L)
        schedule(ctx, "Task ending soon", "$taskName ends in $minutesBefore minutes", alarmTime.toInt(), alarmTime)
    }

    actual fun scheduleNextTaskNotification(taskName: String, timestampMillis: Long) {
        val ctx = context ?: return
        schedule(ctx, "Next task", "$taskName is starting now", timestampMillis.hashCode(), timestampMillis)
    }

    actual fun cancelAllNotifications() {
        val ctx = context ?: return
        val alarmManager = ctx.getSystemService(AlarmManager::class.java)
        for (id in 1..100) {
            val intent = Intent(ctx, NotificationReceiver::class.java)
            val pendingIntent = android.app.PendingIntent.getBroadcast(
                ctx, id, intent,
                android.app.PendingIntent.FLAG_NO_CREATE or android.app.PendingIntent.FLAG_IMMUTABLE,
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }
    }

    actual fun cancelNotification(id: Int) {
        val ctx = context ?: return
        val alarmManager = ctx.getSystemService(AlarmManager::class.java)
        val intent = Intent(ctx, NotificationReceiver::class.java)
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            ctx, id, intent,
            android.app.PendingIntent.FLAG_NO_CREATE or android.app.PendingIntent.FLAG_IMMUTABLE,
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    private fun schedule(ctx: Context, title: String, message: String, id: Int, triggerTime: Long) {
        if (triggerTime < System.currentTimeMillis()) return

        val alarmManager = ctx.getSystemService(AlarmManager::class.java)
        val intent = Intent(ctx, NotificationReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("message", message)
            putExtra("notification_id", id)
        }
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            ctx, id, intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE,
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent
            )
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
        }
    }
}
