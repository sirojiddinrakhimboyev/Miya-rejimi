package com.example.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.entity.DailyTask
import com.example.receiver.AlarmReceiver
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object AlarmScheduler {
    private const val TAG = "AlarmScheduler"

    fun scheduleTaskAlarm(context: Context, task: DailyTask) {
        if (!task.isEnabled || task.isCompleted) {
            cancelTaskAlarm(context, task.id)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val triggerTimeMs = if (task.isSnoozed && task.snoozedUntilTime != null) {
            task.snoozedUntilTime
        } else {
            val baseTime = calculateTriggerMillis(task.date, task.targetHour, task.targetMinute)
            if (task.reminderOffsetMinutes > 0) {
                baseTime - (task.reminderOffsetMinutes * 60 * 1000L)
            } else {
                baseTime
            }
        }

        val now = System.currentTimeMillis()
        if (triggerTimeMs <= now) {
            // Task notification time has already passed
            return
        }

        val desc = if (task.reminderOffsetMinutes > 0 && !task.isSnoozed) {
            "${task.description} (${task.reminderOffsetMinutes} daqiqa oldin eslatildi • Vaqti: ${task.getFormattedTime()})"
        } else {
            task.description
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_TASK_ID, task.id)
            putExtra(AlarmReceiver.EXTRA_TASK_KEY, task.taskKey)
            putExtra(AlarmReceiver.EXTRA_TASK_TITLE, task.title)
            putExtra(AlarmReceiver.EXTRA_TASK_DESC, desc)
            putExtra(AlarmReceiver.EXTRA_TASK_CAT, task.category)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMs,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTimeMs,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMs,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTimeMs,
                    pendingIntent
                )
            }
            Log.d(TAG, "Scheduled alarm for ${task.title} at $triggerTimeMs (diff: ${(triggerTimeMs - now) / 1000}s)")
        } catch (e: SecurityException) {
            Log.e(TAG, "Exact alarm permission missing, fallback", e)
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerTimeMs,
                pendingIntent
            )
        }
    }

    fun scheduleSnoozeAlarm(
        context: Context,
        taskId: Long,
        title: String,
        description: String,
        category: String,
        delayMinutes: Int = 15
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val triggerTimeMs = System.currentTimeMillis() + (delayMinutes * 60 * 1000L)

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_TASK_ID, taskId)
            putExtra(AlarmReceiver.EXTRA_TASK_KEY, "SNOOZE_$taskId")
            putExtra(AlarmReceiver.EXTRA_TASK_TITLE, title)
            putExtra(AlarmReceiver.EXTRA_TASK_DESC, "$description (Eslatma: $delayMinutes daqiqa o'tdi)")
            putExtra(AlarmReceiver.EXTRA_TASK_CAT, category)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
        } else {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
        }
    }

    fun cancelTaskAlarm(context: Context, taskId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    private fun calculateTriggerMillis(dateStr: String, hour: Int, minute: Int): Long {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(dateStr) ?: return System.currentTimeMillis()
            val calendar = Calendar.getInstance().apply {
                time = date
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            calendar.timeInMillis
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }
}
