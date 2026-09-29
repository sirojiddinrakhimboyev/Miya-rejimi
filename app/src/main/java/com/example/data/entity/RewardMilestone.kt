package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reward_milestones")
data class RewardMilestone(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val milestoneKey: String, // "DAY_1", "WEEK_1", "MONTH_1", "YEAR_1"
    val title: String,
    val requiredStreakDays: Int,
    val rankTitle: String,
    val customReward: String, // Foydalanuvchi o'ziga belgilagan rag'bat
    val defaultSuggestion: String,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null,
    val iconEmoji: String
)
