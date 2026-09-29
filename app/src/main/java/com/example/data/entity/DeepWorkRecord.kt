package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deep_work_records")
data class DeepWorkRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val durationMinutes: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val date: String
)
