package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.data.AppDatabase
import com.example.data.preference.PreferenceManager
import com.example.service.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        val title = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Miya Rejimi Vazifasi"
        val desc = intent.getStringExtra(EXTRA_TASK_DESC) ?: "Belgilangan vaqt bo'ldi!"
        val category = intent.getStringExtra(EXTRA_TASK_CAT) ?: "Kunlik tartib"

        val prefs = PreferenceManager(context)
        if (!prefs.notificationsEnabled) {
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val db = AppDatabase.getDatabase(context)
            if (taskId > 0) {
                val task = db.taskDao().getTaskById(taskId)
                if (task != null && task.isCompleted) {
                    // Task already marked completed by user, do not alert
                    return@launch
                }
            }

            NotificationHelper.createNotificationChannel(context)
            NotificationHelper.showTaskNotification(
                context = context,
                taskId = if (taskId > 0) taskId else System.currentTimeMillis() % 100000,
                title = title,
                description = desc,
                category = category
            )
        }
    }

    companion object {
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TASK_KEY = "extra_task_key"
        const val EXTRA_TASK_TITLE = "extra_task_title"
        const val EXTRA_TASK_DESC = "extra_task_desc"
        const val EXTRA_TASK_CAT = "extra_task_cat"
    }
}
