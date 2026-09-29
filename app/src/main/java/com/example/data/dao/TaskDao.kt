package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.DailyTask
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM daily_tasks WHERE date = :date ORDER BY targetHour ASC, targetMinute ASC, orderIndex ASC")
    fun getTasksForDate(date: String): Flow<List<DailyTask>>

    @Query("SELECT * FROM daily_tasks WHERE date = :date ORDER BY targetHour ASC, targetMinute ASC, orderIndex ASC")
    suspend fun getTasksForDateSync(date: String): List<DailyTask>

    @Query("SELECT * FROM daily_tasks WHERE id = :id LIMIT 1")
    suspend fun getTaskById(id: Long): DailyTask?

    @Query("SELECT * FROM daily_tasks WHERE taskKey = :key AND date = :date LIMIT 1")
    suspend fun getTaskByKeyAndDate(key: String, date: String): DailyTask?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: DailyTask): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<DailyTask>)

    @Update
    suspend fun updateTask(task: DailyTask)

    @Delete
    suspend fun deleteTask(task: DailyTask)

    @Query("DELETE FROM daily_tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    @Query("SELECT COUNT(*) FROM daily_tasks WHERE date = :date AND isCompleted = 1")
    fun getCompletedCountForDate(date: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM daily_tasks WHERE date = :date")
    fun getTotalCountForDate(date: String): Flow<Int>

    @Query("SELECT DISTINCT date FROM daily_tasks ORDER BY date DESC LIMIT 30")
    fun getRecentDates(): Flow<List<String>>

    @Query("SELECT * FROM daily_tasks WHERE date >= :startDate ORDER BY date ASC, targetHour ASC")
    fun getTasksSince(startDate: String): Flow<List<DailyTask>>

    @Query("SELECT * FROM daily_tasks WHERE isCustom = 1 AND (repeatFrequency = 'DAILY' OR repeatFrequency = 'WEEKDAYS') GROUP BY taskKey")
    suspend fun getRecurringCustomTasks(): List<DailyTask>
}
