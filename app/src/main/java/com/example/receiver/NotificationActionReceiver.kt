package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.data.AppDatabase
import com.example.service.AlarmScheduler
import com.example.service.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        val action = intent.action ?: return

        when (action) {
            ACTION_COMPLETE -> {
                NotificationHelper.dismissNotification(context, taskId)
                if (taskId > 0) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val db = AppDatabase.getDatabase(context)
                        val task = db.taskDao().getTaskById(taskId)
                        if (task != null) {
                            if (task.hasProof()) {
                                db.taskDao().updateTask(
                                    task.copy(
                                        isCompleted = true,
                                        completedAt = System.currentTimeMillis()
                                    )
                                )
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "✅ Miya Rejimi: Isbot tasdiqlandi, vazifa bajarildi!", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        context,
                                        "📸 Isbot talab qilinadi! Rasm yuklanmasa vazifa bajarilmagan deb hisoblanadi.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                                        putExtra("EXTRA_NAV_TAB", 0)
                                        putExtra("EXTRA_PROMPT_PROOF_TASK_ID", taskId)
                                    }
                                    if (launchIntent != null) {
                                        context.startActivity(launchIntent)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            ACTION_SNOOZE -> {
                val title = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Vazifa"
                val desc = intent.getStringExtra(EXTRA_TASK_DESC) ?: "15 daqiqadan so'ng yana eslatiladi"
                val cat = intent.getStringExtra(EXTRA_TASK_CAT) ?: "Kunlik tartib"

                NotificationHelper.dismissNotification(context, taskId)

                // Schedule snooze in 15 minutes
                val snoozeTimeMs = System.currentTimeMillis() + 15 * 60 * 1000L
                AlarmScheduler.scheduleSnoozeAlarm(
                    context = context,
                    taskId = taskId,
                    title = title,
                    description = desc,
                    category = cat,
                    delayMinutes = 15
                )

                if (taskId > 0) {
                    CoroutineScope(Dispatchers.IO).launch {
                        val db = AppDatabase.getDatabase(context)
                        val task = db.taskDao().getTaskById(taskId)
                        if (task != null) {
                            db.taskDao().updateTask(
                                task.copy(
                                    isSnoozed = true,
                                    snoozedUntilTime = snoozeTimeMs
                                )
                            )
                        }
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "⏱ 15 daqiqaga qoldirildi", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val ACTION_COMPLETE = "com.example.miya_rejimi.ACTION_COMPLETE"
        const val ACTION_SNOOZE = "com.example.miya_rejimi.ACTION_SNOOZE"

        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TASK_TITLE = "extra_task_title"
        const val EXTRA_TASK_DESC = "extra_task_desc"
        const val EXTRA_TASK_CAT = "extra_task_cat"
    }
}
