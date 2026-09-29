package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Locale

@Entity(tableName = "daily_tasks")
data class DailyTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskKey: String,
    val title: String,
    val category: String,
    val description: String,
    val targetHour: Int,
    val targetMinute: Int,
    val date: String,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val isSnoozed: Boolean = false,
    val snoozedUntilTime: Long? = null,
    val isCustom: Boolean = false,
    val orderIndex: Int = 0,
    val isEnabled: Boolean = true,
    val reminderOffsetMinutes: Int = 0, // 0 = Aynan vaqtida, 5, 10, 15, 30 daqiqa oldin
    val repeatFrequency: String = "ONCE", // "ONCE" (Bir marta), "DAILY" (Har kuni), "WEEKDAYS" (Ish kunlari)
    val proofPhotoUri: String? = null // Rasm orqali isbot (Rasm bo'lmasa vazifa bajarilmagan deb hisoblanadi)
) {
    fun hasProof(): Boolean = !proofPhotoUri.isNullOrBlank()
    fun getFormattedTime(): String {
        return String.format(Locale.getDefault(), "%02d:%02d", targetHour, targetMinute)
    }

    fun getReminderLabel(): String {
        return when (reminderOffsetMinutes) {
            0 -> "Aynan vaqtida"
            5 -> "5 daq oldin"
            10 -> "10 daq oldin"
            15 -> "15 daq oldin"
            30 -> "30 daq oldin"
            else -> "$reminderOffsetMinutes daq oldin"
        }
    }

    fun getFrequencyLabel(): String {
        return when (repeatFrequency) {
            "DAILY" -> "Har kuni"
            "WEEKDAYS" -> "Dush-Juma"
            else -> "Bir martalik"
        }
    }
}
