package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.ReactionScore
import kotlinx.coroutines.flow.Flow

@Dao
interface ReactionDao {
    @Query("SELECT * FROM reaction_scores ORDER BY timestamp DESC")
    fun getAllScores(): Flow<List<ReactionScore>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: ReactionScore): Long

    @Query("SELECT * FROM reaction_scores ORDER BY timestamp DESC LIMIT 1")
    fun getLatestScore(): Flow<ReactionScore?>
}
