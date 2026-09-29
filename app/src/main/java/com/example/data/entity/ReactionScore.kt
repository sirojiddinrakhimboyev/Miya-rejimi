package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reaction_scores")
data class ReactionScore(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val reactionMs: Long,
    val rating: String,
    val timestamp: Long = System.currentTimeMillis(),
    val date: String
)
