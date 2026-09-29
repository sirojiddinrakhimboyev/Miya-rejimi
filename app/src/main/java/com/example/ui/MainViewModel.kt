package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.GeminiService
import com.example.data.AppDatabase
import com.example.data.entity.ChatMessage
import com.example.data.entity.DailyTask
import com.example.data.entity.DeepWorkRecord
import com.example.data.entity.ReactionScore
import com.example.data.entity.RewardMilestone
import com.example.data.preference.PreferenceManager
import com.example.data.repository.TaskRepository
import com.example.service.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    private val database = AppDatabase.getDatabase(context)
    val prefs = PreferenceManager(context)
    private val repository = TaskRepository(context, database, prefs)
    private val geminiService = GeminiService()

    private val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayDate: String = sdf.format(Date())

    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    private val _isOnboardingCompleted = MutableStateFlow(prefs.isOnboardingCompleted)
    val isOnboardingCompleted: StateFlow<Boolean> = _isOnboardingCompleted.asStateFlow()

    val wakeHour = MutableStateFlow(prefs.wakeHour)
    val wakeMinute = MutableStateFlow(prefs.wakeMinute)
    val sleepHour = MutableStateFlow(prefs.sleepHour)
    val sleepMinute = MutableStateFlow(prefs.sleepMinute)
    val notificationsEnabled = MutableStateFlow(prefs.notificationsEnabled)

    val streak = MutableStateFlow(prefs.currentStreak)
    val bestStreak = MutableStateFlow(prefs.bestStreak)
    val isAdmin = MutableStateFlow(prefs.isAdmin)
    val isPremium = MutableStateFlow(prefs.isPremium)
    val adminAnnouncement = MutableStateFlow(prefs.adminAnnouncement)

    val todayTasks: StateFlow<List<DailyTask>> = repository.getTasksForDate(todayDate)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val completedCount: StateFlow<Int> = todayTasks.map { tasks ->
        tasks.count { it.isCompleted }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalCount: StateFlow<Int> = todayTasks.map { tasks ->
        tasks.count { it.isEnabled }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val nextActiveTask: StateFlow<DailyTask?> = todayTasks.map { tasks ->
        val nowCal = Calendar.getInstance()
        val currentMinutes = nowCal.get(Calendar.HOUR_OF_DAY) * 60 + nowCal.get(Calendar.MINUTE)

        val uncompleted = tasks.filter { it.isEnabled && !it.isCompleted }
        // Find task closest to or right after current time, or first uncompleted
        uncompleted.firstOrNull { (it.targetHour * 60 + it.targetMinute) >= currentMinutes }
            ?: uncompleted.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val reactionScores: StateFlow<List<ReactionScore>> = database.reactionDao().getAllScores()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deepWorkRecords: StateFlow<List<DeepWorkRecord>> = database.deepWorkDao().getAllRecords()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessage>> = database.chatDao().getAllMessages()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val milestones: StateFlow<List<RewardMilestone>> = database.rewardDao().getAllMilestones()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private val _taskForProofDialog = MutableStateFlow<DailyTask?>(null)
    val taskForProofDialog: StateFlow<DailyTask?> = _taskForProofDialog.asStateFlow()

    private val _taskForViewProof = MutableStateFlow<DailyTask?>(null)
    val taskForViewProof: StateFlow<DailyTask?> = _taskForViewProof.asStateFlow()

    private val _proofNotice = MutableStateFlow<String?>(null)
    val proofNotice: StateFlow<String?> = _proofNotice.asStateFlow()

    fun openProofDialog(task: DailyTask) {
        _taskForProofDialog.value = task
    }

    fun promptProofForTaskId(taskId: Long) {
        viewModelScope.launch {
            val task = repository.getTaskById(taskId)
            if (task != null) {
                openProofDialog(task)
            }
        }
    }

    fun closeProofDialog() {
        _taskForProofDialog.value = null
    }

    fun openViewProof(task: DailyTask) {
        _taskForViewProof.value = task
    }

    fun closeViewProof() {
        _taskForViewProof.value = null
    }

    fun clearProofNotice() {
        _proofNotice.value = null
    }

    init {
        updateStreak()
        viewModelScope.launch {
            initDefaultMilestonesIfEmpty()
            checkMilestoneUnlocks()
            repository.generateDailyScheduleIfMissing(todayDate)
        }
    }

    private suspend fun initDefaultMilestonesIfEmpty() {
        val existing = database.rewardDao().getAllMilestonesSync()
        if (existing.isEmpty()) {
            val defaults = listOf(
                RewardMilestone(
                    milestoneKey = "DAY_1",
                    title = "1 Kunlik To'liq G'alaba",
                    requiredStreakDays = 1,
                    rankTitle = "Yangi Boshlovchi 🌱",
                    customReward = "Sevimli qahva yoki film tomosha qilish",
                    defaultSuggestion = "Sevimli qahva yoki film tomosha qilish",
                    iconEmoji = "🌱"
                ),
                RewardMilestone(
                    milestoneKey = "WEEK_1",
                    title = "1 Haftalik Intizom (7 kun)",
                    requiredStreakDays = 7,
                    rankTitle = "Intizomli Biohaker ⚡",
                    customReward = "Dam olish kuni yoki mini-sovg'a",
                    defaultSuggestion = "Dam olish kuni yoki mini-sovg'a",
                    iconEmoji = "🔥"
                ),
                RewardMilestone(
                    milestoneKey = "MONTH_1",
                    title = "1 Oylik Temir Iroda (30 kun)",
                    requiredStreakDays = 30,
                    rankTitle = "Neyroplastik Usta 🧠",
                    customReward = "Yangi kitob yoki kiyim xaridi",
                    defaultSuggestion = "Yangi kitob yoki kiyim xaridi",
                    iconEmoji = "💎"
                ),
                RewardMilestone(
                    milestoneKey = "YEAR_1",
                    title = "1 Yillik Hayot Tarzi (365 kun)",
                    requiredStreakDays = 365,
                    rankTitle = "Mutlaq Miya Ustasi 👑",
                    customReward = "Katta sayohat yoki orzudagi yirik xarid",
                    defaultSuggestion = "Katta sayohat yoki orzudagi yirik xarid",
                    iconEmoji = "👑"
                )
            )
            database.rewardDao().insertMilestones(defaults)
        }
    }

    private suspend fun checkMilestoneUnlocks() {
        val current = prefs.currentStreak
        val existing = database.rewardDao().getAllMilestonesSync()
        for (m in existing) {
            if (!m.isUnlocked && current >= m.requiredStreakDays) {
                database.rewardDao().updateMilestone(
                    m.copy(
                        isUnlocked = true,
                        unlockedAt = System.currentTimeMillis()
                    )
                )
            }
        }
    }

    fun updateMilestoneReward(milestoneId: Long, newReward: String) {
        viewModelScope.launch {
            val list = database.rewardDao().getAllMilestonesSync()
            val target = list.firstOrNull { it.id == milestoneId }
            if (target != null) {
                database.rewardDao().updateMilestone(
                    target.copy(customReward = newReward)
                )
            }
        }
    }

    private fun updateStreak() {
        val lastDate = prefs.lastActiveDate
        val cal = Calendar.getInstance()
        val todayStr = sdf.format(cal.time)

        if (lastDate.isEmpty()) {
            prefs.lastActiveDate = todayStr
            prefs.currentStreak = 1
            if (prefs.bestStreak < 1) prefs.bestStreak = 1
        } else if (lastDate != todayStr) {
            cal.add(Calendar.DAY_OF_YEAR, -1)
            val yesterdayStr = sdf.format(cal.time)
            if (lastDate == yesterdayStr) {
                // Kept streak alive
                prefs.lastActiveDate = todayStr
            } else {
                // Missed more than 1 day
                prefs.currentStreak = 1
                prefs.lastActiveDate = todayStr
            }
        }
        streak.value = prefs.currentStreak
        bestStreak.value = prefs.bestStreak
    }

    fun selectTab(index: Int) {
        _selectedTab.value = index
    }

    fun completeOnboarding(wakeH: Int, wakeM: Int, sleepH: Int, sleepM: Int) {
        prefs.wakeHour = wakeH
        prefs.wakeMinute = wakeM
        prefs.sleepHour = sleepH
        prefs.sleepMinute = sleepM
        prefs.isOnboardingCompleted = true

        wakeHour.value = wakeH
        wakeMinute.value = wakeM
        sleepHour.value = sleepH
        sleepMinute.value = sleepM
        _isOnboardingCompleted.value = true

        viewModelScope.launch {
            repository.regenerateSchedule(wakeH, wakeM, sleepH, sleepM)
        }
    }

    fun markTaskCompleted(task: DailyTask, completed: Boolean) {
        if (completed) {
            // Rasm orqali isbot talab qilinadi!
            if (task.hasProof()) {
                viewModelScope.launch {
                    repository.markTaskCompleted(task.id, true, task.proofPhotoUri)
                }
            } else {
                openProofDialog(task)
            }
        } else {
            uncompleteTask(task)
        }
    }

    fun completeTaskWithProof(task: DailyTask, photoUri: String) {
        viewModelScope.launch {
            if (photoUri.isBlank()) {
                rejectTaskWithoutProof(task)
                return@launch
            }
            val success = repository.markTaskCompleted(task.id, true, photoUri)
            if (success) {
                _proofNotice.value = "📸 Isbot qabul qilindi: «${task.title}» bajarildi!"
                val newCompletedCount = (todayTasks.value.count { it.isCompleted } + 1)
                val total = todayTasks.value.count { it.isEnabled }
                if (total > 0 && newCompletedCount >= (total * 0.7)) {
                    val curr = prefs.currentStreak
                    if (prefs.lastActiveDate != todayDate) {
                        prefs.currentStreak = curr + 1
                        if (prefs.currentStreak > prefs.bestStreak) {
                            prefs.bestStreak = prefs.currentStreak
                        }
                        prefs.lastActiveDate = todayDate
                        streak.value = prefs.currentStreak
                        bestStreak.value = prefs.bestStreak
                        checkMilestoneUnlocks()
                    }
                }
            } else {
                _proofNotice.value = "❌ Rasm bo'lmagani uchun vazifa bajarilmagan deb belgilandi!"
            }
            _taskForProofDialog.value = null
        }
    }

    fun rejectTaskWithoutProof(task: DailyTask) {
        viewModelScope.launch {
            repository.markTaskCompleted(task.id, false, null)
            _proofNotice.value = "⚠️ Rasm yuklanmadi — «${task.title}» bajarilmagan deb belgilandi."
            _taskForProofDialog.value = null
        }
    }

    fun uncompleteTask(task: DailyTask) {
        viewModelScope.launch {
            repository.markTaskCompleted(task.id, false, null)
            _proofNotice.value = "Vazifa bajarilmagan holatga qaytarildi"
            _taskForViewProof.value = null
        }
    }

    fun snoozeTask(task: DailyTask, minutes: Int = 15) {
        viewModelScope.launch {
            repository.snoozeTask(task.id, minutes)
        }
    }

    fun addCustomTask(
        title: String,
        category: String,
        description: String,
        hour: Int,
        minute: Int,
        reminderOffsetMinutes: Int = 0,
        repeatFrequency: String = "ONCE"
    ) {
        viewModelScope.launch {
            repository.addCustomTask(
                title = title,
                category = category,
                description = description,
                hour = hour,
                minute = minute,
                date = todayDate,
                reminderOffsetMinutes = reminderOffsetMinutes,
                repeatFrequency = repeatFrequency
            )
        }
    }

    fun sendAiQuestion(question: String) {
        val trimmed = question.trim()
        if (trimmed.isBlank() || _isAiLoading.value) return

        viewModelScope.launch {
            database.chatDao().insertMessage(ChatMessage(text = trimmed, isUser = true))
            _isAiLoading.value = true
            try {
                val answer = geminiService.askAdvisor(trimmed)
                database.chatDao().insertMessage(ChatMessage(text = answer, isUser = false))
            } catch (e: Exception) {
                database.chatDao().insertMessage(
                    ChatMessage(
                        text = "Kechirasiz, javob olishda xatolik yuz berdi. Iltimos qaytadan urinib ko'ring.",
                        isUser = false
                    )
                )
            } finally {
                _isAiLoading.value = false
            }
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            database.chatDao().clearHistory()
        }
    }

    fun deleteTask(taskId: Long) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
        }
    }

    fun saveRoutineSettings(wakeH: Int, wakeM: Int, sleepH: Int, sleepM: Int) {
        wakeHour.value = wakeH
        wakeMinute.value = wakeM
        sleepHour.value = sleepH
        sleepMinute.value = sleepM

        viewModelScope.launch {
            repository.regenerateSchedule(wakeH, wakeM, sleepH, sleepM)
        }
    }

    fun toggleRoutineItem(taskKey: String, isEnabled: Boolean) {
        prefs.setRoutineEnabled(taskKey, isEnabled)
        viewModelScope.launch {
            repository.regenerateSchedule(
                prefs.wakeHour,
                prefs.wakeMinute,
                prefs.sleepHour,
                prefs.sleepMinute
            )
        }
    }

    fun toggleNotifications(enabled: Boolean) {
        prefs.notificationsEnabled = enabled
        notificationsEnabled.value = enabled
    }

    fun testNotification() {
        NotificationHelper.createNotificationChannel(context)
        NotificationHelper.showTaskNotification(
            context = context,
            taskId = 9999L,
            title = "Test Buyruq: 10 daqiqa quyosh nuriga chiq!",
            description = "Miya Rejimi signallari a'lo darajada ishlamoqda. Ekran o'chiq bo'lsa ham buyruq beriladi.",
            category = "Miya Rejimi Sinovi"
        )
    }

    fun recordReactionScore(scoreMs: Long) {
        val rating = when {
            scoreMs < 250 -> "Super tetik 🚀"
            scoreMs < 320 -> "Yaxshi holatda ⚡"
            scoreMs < 400 -> "O'rtacha diqqat 💡"
            else -> "Charchagan / Dam kerak 😴"
        }
        viewModelScope.launch {
            database.reactionDao().insertScore(
                ReactionScore(
                    reactionMs = scoreMs,
                    rating = rating,
                    date = todayDate
                )
            )
        }
    }

    fun recordDeepWorkSession(durationMinutes: Int) {
        viewModelScope.launch {
            database.deepWorkDao().insertRecord(
                DeepWorkRecord(
                    durationMinutes = durationMinutes,
                    date = todayDate
                )
            )
        }
    }

    fun setAdmin(enabled: Boolean) {
        prefs.isAdmin = enabled
        isAdmin.value = enabled
    }

    fun setPremium(enabled: Boolean) {
        prefs.isPremium = enabled
        isPremium.value = enabled
    }

    fun setAdminAnnouncement(text: String) {
        prefs.adminAnnouncement = text
        adminAnnouncement.value = text
    }
}
