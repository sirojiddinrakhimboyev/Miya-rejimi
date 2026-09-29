package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.receiver.NotificationActionReceiver

object NotificationHelper {
    const val CHANNEL_ID = "miya_rejimi_protocol"
    const val CHANNEL_NAME = "Miya Rejimi Eslatmalari"
    const val NOTIFICATION_GROUP = "com.example.miya_rejimi.NOTIFICATIONS"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Miya Rejimi kunlik intizom va buyruq bildirishnomalari"
                enableLights(true)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 350, 150, 350)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showTaskNotification(
        context: Context,
        taskId: Long,
        title: String,
        description: String,
        category: String
    ) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Open app when clicked
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_NAV_TAB", 0)
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: "Bajardim"
        val doneIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_COMPLETE
            putExtra(NotificationActionReceiver.EXTRA_TASK_ID, taskId)
        }
        val donePendingIntent = PendingIntent.getBroadcast(
            context,
            (taskId * 10 + 1).toInt(),
            doneIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: "Keyinroq" (15 min delay)
        val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_SNOOZE
            putExtra(NotificationActionReceiver.EXTRA_TASK_ID, taskId)
            putExtra(NotificationActionReceiver.EXTRA_TASK_TITLE, title)
            putExtra(NotificationActionReceiver.EXTRA_TASK_DESC, description)
            putExtra(NotificationActionReceiver.EXTRA_TASK_CAT, category)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            (taskId * 10 + 2).toInt(),
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle("🧠 $title")
            .setContentText(description)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$description\n\nBo'lim: $category")
                    .setSummaryText("Miya Rejimi")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 300, 150, 300))
            .setContentIntent(contentPendingIntent)
            .addAction(
                android.R.drawable.checkbox_on_background,
                "✅ Bajardim",
                donePendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_recent_history,
                "⏱ Keyinroq (15 daq)",
                snoozePendingIntent
            )

        notificationManager.notify(taskId.toInt(), builder.build())
    }

    fun dismissNotification(context: Context, taskId: Long) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(taskId.toInt())
    }
}
