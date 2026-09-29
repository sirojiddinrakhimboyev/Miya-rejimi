package com.example.data.repository

import android.content.Context
import com.example.data.AppDatabase
import com.example.data.entity.DailyTask
import com.example.data.preference.PreferenceManager
import com.example.service.AlarmScheduler
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class TaskRepository(
    private val context: Context,
    private val database: AppDatabase,
    private val prefs: PreferenceManager
) {
    private val taskDao = database.taskDao()

    fun getTasksForDate(date: String): Flow<List<DailyTask>> {
        return taskDao.getTasksForDate(date)
    }

    suspend fun getTaskById(id: Long): DailyTask? {
        return taskDao.getTaskById(id)
    }

    suspend fun markTaskCompleted(taskId: Long, completed: Boolean, photoUri: String? = null): Boolean {
        val task = taskDao.getTaskById(taskId) ?: return false
        if (!completed) {
            val updated = task.copy(
                isCompleted = false,
                completedAt = null,
                proofPhotoUri = null
            )
            taskDao.updateTask(updated)
            AlarmScheduler.scheduleTaskAlarm(context, updated)
            return true
        } else {
            val finalUri = photoUri ?: task.proofPhotoUri
            if (finalUri.isNullOrBlank()) {
                // Isbot bo'lmasa, dastur vazifa bajarilmaganini belgilaydi
                val uncompleted = task.copy(
                    isCompleted = false,
                    completedAt = null,
                    proofPhotoUri = null
                )
                taskDao.updateTask(uncompleted)
                return false
            }
            val updated = task.copy(
                isCompleted = true,
                completedAt = System.currentTimeMillis(),
                proofPhotoUri = finalUri
            )
            taskDao.updateTask(updated)
            AlarmScheduler.cancelTaskAlarm(context, taskId)
            return true
        }
    }

    suspend fun snoozeTask(taskId: Long, delayMinutes: Int = 15) {
        val task = taskDao.getTaskById(taskId) ?: return
        val snoozeTimeMs = System.currentTimeMillis() + delayMinutes * 60 * 1000L
        val updated = task.copy(
            isSnoozed = true,
            snoozedUntilTime = snoozeTimeMs
        )
        taskDao.updateTask(updated)
        AlarmScheduler.scheduleSnoozeAlarm(
            context = context,
            taskId = task.id,
            title = task.title,
            description = task.description,
            category = task.category,
            delayMinutes = delayMinutes
        )
    }

    suspend fun addCustomTask(
        title: String,
        category: String,
        description: String,
        hour: Int,
        minute: Int,
        date: String,
        reminderOffsetMinutes: Int = 0,
        repeatFrequency: String = "ONCE"
    ): Long {
        val task = DailyTask(
            taskKey = "CUSTOM_${System.currentTimeMillis()}",
            title = title,
            category = category.ifBlank { "Shaxsiy" },
            description = description.ifBlank { "Shaxsiy reja yoki uchrashuv" },
            targetHour = hour,
            targetMinute = minute,
            date = date,
            isCompleted = false,
            isCustom = true,
            orderIndex = 100,
            reminderOffsetMinutes = reminderOffsetMinutes,
            repeatFrequency = repeatFrequency
        )
        val id = taskDao.insertTask(task)
        val savedTask = task.copy(id = id)
        AlarmScheduler.scheduleTaskAlarm(context, savedTask)
        return id
    }

    suspend fun deleteTask(taskId: Long) {
        AlarmScheduler.cancelTaskAlarm(context, taskId)
        taskDao.deleteTaskById(taskId)
    }

    suspend fun generateDailyScheduleIfMissing(dateStr: String) {
        val existing = taskDao.getTasksForDateSync(dateStr)
        // If system routine tasks already generated for this date, do not recreate system items
        val hasSystemTasks = existing.any { !it.isCustom }
        if (!hasSystemTasks) {
            val wakeHour = prefs.wakeHour
            val wakeMinute = prefs.wakeMinute
            val sleepHour = prefs.sleepHour
            val sleepMinute = prefs.sleepMinute

            val protocolList = buildProtocolList(wakeHour, wakeMinute, sleepHour, sleepMinute, dateStr)
            for (item in protocolList) {
                val isEnabled = prefs.isRoutineEnabled(item.taskKey)
                val taskToInsert = item.copy(isEnabled = isEnabled)
                val id = taskDao.insertTask(taskToInsert)
                val savedTask = taskToInsert.copy(id = id)
                if (isEnabled && prefs.notificationsEnabled) {
                    AlarmScheduler.scheduleTaskAlarm(context, savedTask)
                }
            }
        }

        // Check and copy recurring custom tasks if not yet added for today
        try {
            val calendar = Calendar.getInstance()
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            val isWeekday = dayOfWeek in Calendar.MONDAY..Calendar.FRIDAY

            val recurringTasks = taskDao.getRecurringCustomTasks()
            val todayKeys = existing.map { it.taskKey }.toSet()

            for (rec in recurringTasks) {
                if (todayKeys.contains(rec.taskKey)) continue
                val shouldInclude = when (rec.repeatFrequency) {
                    "DAILY" -> true
                    "WEEKDAYS" -> isWeekday
                    else -> false
                }
                if (shouldInclude) {
                    val cloned = rec.copy(
                        id = 0,
                        date = dateStr,
                        isCompleted = false,
                        completedAt = null,
                        isSnoozed = false,
                        snoozedUntilTime = null
                    )
                    val id = taskDao.insertTask(cloned)
                    val saved = cloned.copy(id = id)
                    if (saved.isEnabled && prefs.notificationsEnabled) {
                        AlarmScheduler.scheduleTaskAlarm(context, saved)
                    }
                }
            }
        } catch (_: Exception) {}
    }

    suspend fun regenerateSchedule(wakeHour: Int, wakeMinute: Int, sleepHour: Int, sleepMinute: Int) {
        prefs.wakeHour = wakeHour
        prefs.wakeMinute = wakeMinute
        prefs.sleepHour = sleepHour
        prefs.sleepMinute = sleepMinute

        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val existing = taskDao.getTasksForDateSync(today)
        val customTasks = existing.filter { it.isCustom }

        // Cancel old alarms for today's non-custom tasks
        existing.filter { !it.isCustom }.forEach {
            AlarmScheduler.cancelTaskAlarm(context, it.id)
            taskDao.deleteTask(it)
        }

        // Recreate protocol
        val newProtocol = buildProtocolList(wakeHour, wakeMinute, sleepHour, sleepMinute, today)
        for (item in newProtocol) {
            val isEnabled = prefs.isRoutineEnabled(item.taskKey)
            val taskToInsert = item.copy(isEnabled = isEnabled)
            val id = taskDao.insertTask(taskToInsert)
            val savedTask = taskToInsert.copy(id = id)
            if (isEnabled && prefs.notificationsEnabled) {
                AlarmScheduler.scheduleTaskAlarm(context, savedTask)
            }
        }

        // Reschedule custom tasks
        customTasks.forEach {
            if (!it.isCompleted) {
                AlarmScheduler.scheduleTaskAlarm(context, it)
            }
        }
    }

    private fun buildProtocolList(
        wakeHour: Int,
        wakeMin: Int,
        sleepHour: Int,
        sleepMin: Int,
        dateStr: String
    ): List<DailyTask> {
        val wakeCal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, wakeHour)
            set(Calendar.MINUTE, wakeMin)
            set(Calendar.SECOND, 0)
        }

        fun timeAfterMinutes(minutes: Int): Pair<Int, Int> {
            val cal = wakeCal.clone() as Calendar
            cal.add(Calendar.MINUTE, minutes)
            return Pair(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
        }

        fun timeBeforeSleep(minutesBefore: Int): Pair<Int, Int> {
            val sleepCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, sleepHour)
                set(Calendar.MINUTE, sleepMin)
                set(Calendar.SECOND, 0)
            }
            sleepCal.add(Calendar.MINUTE, -minutesBefore)
            return Pair(sleepCal.get(Calendar.HOUR_OF_DAY), sleepCal.get(Calendar.MINUTE))
        }

        val list = mutableListOf<DailyTask>()

        // 1. +0 min: Quyosh nuri
        val (h0, m0) = timeAfterMinutes(0)
        list.add(
            DailyTask(
                taskKey = "SUNLIGHT",
                title = "Derazani och, 10 daqiqa quyosh nuriga chiq",
                category = "Quyosh nuri & Sergaklik",
                description = "Ko'zlarga tushgan ertalabki tabiiy nur melatoninni to'xtatadi va miyani uyg'otadi. Ekranga qaramang.",
                targetHour = h0,
                targetMinute = m0,
                date = dateStr,
                orderIndex = 1
            )
        )

        // 2. +60 min: Kofein vaqti
        val (h60, m60) = timeAfterMinutes(60)
        list.add(
            DailyTask(
                taskKey = "COFFEINE_ON",
                title = "Kofeinni hozir ichishing mumkin",
                category = "Kofein vaqti",
                description = "Uyg'ongandan 60 daqiqa o'tgach adenozin retseptorlari barqarorlashadi. Kofein maksimal quvvat beradi.",
                targetHour = h60,
                targetMinute = m60,
                date = dateStr,
                orderIndex = 2
            )
        )

        // 3. +2 soat: Chuqur ish
        val (h120, m120) = timeAfterMinutes(120)
        list.add(
            DailyTask(
                taskKey = "DEEP_WORK",
                title = "Eng qiyin ishni boshla. Telefonni boshqa xonaga qo'y. 60-90 daqiqa chuqur ish",
                category = "Chuqur ish",
                description = "Miyaning kognitiv energiyasi eng yuqori nuqtada. Diqqatni 100% bitta asosiy maqsadga qarating.",
                targetHour = h120,
                targetMinute = m120,
                date = dateStr,
                orderIndex = 3
            )
        )

        // 4. Chuqur ishdan keyin: Dam olish
        val (h210, m210) = timeAfterMinutes(210)
        list.add(
            DailyTask(
                taskKey = "REST",
                title = "10-15 daqiqa dam ol, ekransiz yur",
                category = "Miyani bo'shatish",
                description = "Dopamin va miya neyronlarini tiklash uchun ko'zlarni dam oldiring, suv iching, bir oz harakat qiling.",
                targetHour = h210,
                targetMinute = m210,
                date = dateStr,
                orderIndex = 4
            )
        )

        // 5. Kun o'rtasi: Mashq / Yurish
        val (h300, m300) = timeAfterMinutes(300)
        list.add(
            DailyTask(
                taskKey = "WORKOUT",
                title = "30 daqiqa yurish yoki mashq",
                category = "Jismoniy faollik",
                description = "Qon aylanishini kuchaytirish va miyaga kislorod yetkazish uchun tez yurish yoki mashq (haftasiga 3-5 kun).",
                targetHour = h300,
                targetMinute = m300,
                date = dateStr,
                orderIndex = 5
            )
        )

        // 6. 14:00 da: Kofein tugadi
        list.add(
            DailyTask(
                taskKey = "COFFEINE_OFF",
                title = "Kofein tugadi, endi ichma",
                category = "Kofein chegarasi",
                description = "Kofeinning yarim yemirilish muddati 6-8 soat. Kechasi chuqur va sifatli uxlash uchun oxirgi chegara.",
                targetHour = 14,
                targetMinute = 0,
                date = dateStr,
                orderIndex = 6
            )
        )

        // 7. Ixtiyoriy: 20 daqiqa uyqu
        val (h450, m450) = timeAfterMinutes(450)
        val napHour = if (h450 < 13 || h450 > 16) 14 else h450
        val napMin = if (h450 < 13 || h450 > 16) 30 else m450
        list.add(
            DailyTask(
                taskKey = "NAP",
                title = "20 daqiqalik uyqu (30 daqiqadan oshirma)",
                category = "Qisqa quvvat uyqusi",
                description = "Ixtiyoriy: tetiklikni 100% tiklaydi. Chuqur uyqu fazasiga o'tib ketmaslik uchun taymerni 20 daqiqaga qo'ying.",
                targetHour = napHour,
                targetMinute = napMin,
                date = dateStr,
                orderIndex = 7
            )
        )

        // 8. Kechqurun: Ertangi 3 ta ish
        val (hPlan, mPlan) = timeBeforeSleep(150)
        list.add(
            DailyTask(
                taskKey = "PLANNING",
                title = "Ertangi 3 ta asosiy ishni yoz",
                category = "Rejalashtirish",
                description = "Miyadagi ochiq sikllarni qog'ozga tushiring. Bu kechasi miyangiz xotirjam dam olishiga yordam beradi.",
                targetHour = hPlan,
                targetMinute = mPlan,
                date = dateStr,
                orderIndex = 8
            )
        )

        // 9. Uxlashdan 60 daqiqa oldin: Telefonni qo'y
        val (hPhone, mPhone) = timeBeforeSleep(60)
        list.add(
            DailyTask(
                taskKey = "PHONE_AWAY",
                title = "Telefonni qo'y, ko'k chiroqni to'xtat",
                category = "Raqamli detoks",
                description = "Ekran nuridan saqlaning. Iliq dush, kitob o'qish yoki xira sariq yorug'likda dam oling.",
                targetHour = hPhone,
                targetMinute = mPhone,
                date = dateStr,
                orderIndex = 9
            )
        )

        // 10. Uxlash vaqti
        list.add(
            DailyTask(
                taskKey = "SLEEP",
                title = "Uxlash vaqti bo'ldi",
                category = "Sog'lom uyqu",
                description = "Xonani salqin va qorong'i qiling. Miya neyronlari tiklanishi va xotira tozalanishi uchun uxlash vaqti.",
                targetHour = sleepHour,
                targetMinute = sleepMin,
                date = dateStr,
                orderIndex = 10
            )
        )

        return list
    }
}
