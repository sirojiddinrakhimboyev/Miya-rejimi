package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.RewardMilestone
import kotlinx.coroutines.flow.Flow

@Dao
interface RewardDao {
    @Query("SELECT * FROM reward_milestones ORDER BY requiredStreakDays ASC")
    fun getAllMilestones(): Flow<List<RewardMilestone>>

    @Query("SELECT * FROM reward_milestones ORDER BY requiredStreakDays ASC")
    suspend fun getAllMilestonesSync(): List<RewardMilestone>

    @Query("SELECT * FROM reward_milestones WHERE milestoneKey = :key LIMIT 1")
    suspend fun getMilestoneByKey(key: String): RewardMilestone?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestones(milestones: List<RewardMilestone>)

    @Update
    suspend fun updateMilestone(milestone: RewardMilestone)
}
