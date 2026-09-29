package com.example.data.preference

import android.content.Context
import android.content.SharedPreferences

class PreferenceManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("miya_rejimi_prefs", Context.MODE_PRIVATE)

    var wakeHour: Int
        get() = prefs.getInt(KEY_WAKE_HOUR, 7)
        set(value) = prefs.edit().putInt(KEY_WAKE_HOUR, value).apply()

    var wakeMinute: Int
        get() = prefs.getInt(KEY_WAKE_MINUTE, 0)
        set(value) = prefs.edit().putInt(KEY_WAKE_MINUTE, value).apply()

    var sleepHour: Int
        get() = prefs.getInt(KEY_SLEEP_HOUR, 23)
        set(value) = prefs.edit().putInt(KEY_SLEEP_HOUR, value).apply()

    var sleepMinute: Int
        get() = prefs.getInt(KEY_SLEEP_MINUTE, 0)
        set(value) = prefs.edit().putInt(KEY_SLEEP_MINUTE, value).apply()

    var isOnboardingCompleted: Boolean
        get() = prefs.getBoolean(KEY_ONBOARDING_DONE, false)
        set(value) = prefs.edit().putBoolean(KEY_ONBOARDING_DONE, value).apply()

    var notificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, value).apply()

    var currentStreak: Int
        get() = prefs.getInt(KEY_CURRENT_STREAK, 1)
        set(value) = prefs.edit().putInt(KEY_CURRENT_STREAK, value).apply()

    var bestStreak: Int
        get() = prefs.getInt(KEY_BEST_STREAK, 1)
        set(value) = prefs.edit().putInt(KEY_BEST_STREAK, value).apply()

    var lastActiveDate: String
        get() = prefs.getString(KEY_LAST_ACTIVE_DATE, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LAST_ACTIVE_DATE, value).apply()

    var isAdmin: Boolean
        get() = prefs.getBoolean(KEY_IS_ADMIN, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_ADMIN, value).apply()

    var isPremium: Boolean
        get() = prefs.getBoolean(KEY_IS_PREMIUM, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_PREMIUM, value).apply()

    var adminAnnouncement: String
        get() = prefs.getString(KEY_ADMIN_ANNOUNCEMENT, "Admin tavsiyasi: Har kuni tongda 10 daqiqa quyosh nuriga chiqishni unutmang!") ?: ""
        set(value) = prefs.edit().putString(KEY_ADMIN_ANNOUNCEMENT, value).apply()

    fun isRoutineEnabled(key: String): Boolean {
        return prefs.getBoolean("routine_enabled_$key", true)
    }

    fun setRoutineEnabled(key: String, enabled: Boolean) {
        prefs.edit().putBoolean("routine_enabled_$key", enabled).apply()
    }

    companion object {
        private const val KEY_WAKE_HOUR = "wake_hour"
        private const val KEY_WAKE_MINUTE = "wake_minute"
        private const val KEY_SLEEP_HOUR = "sleep_hour"
        private const val KEY_SLEEP_MINUTE = "sleep_minute"
        private const val KEY_ONBOARDING_DONE = "onboarding_done"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_CURRENT_STREAK = "current_streak"
        private const val KEY_BEST_STREAK = "best_streak"
        private const val KEY_LAST_ACTIVE_DATE = "last_active_date"
        private const val KEY_IS_ADMIN = "is_admin"
        private const val KEY_IS_PREMIUM = "is_premium"
        private const val KEY_ADMIN_ANNOUNCEMENT = "admin_announcement"
    }
}
