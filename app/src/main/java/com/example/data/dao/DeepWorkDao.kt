package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.DeepWorkRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface DeepWorkDao {
    @Query("SELECT * FROM deep_work_records ORDER BY timestamp DESC")
    fun getAllRecords(): Flow<List<DeepWorkRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: DeepWorkRecord): Long

    @Query("SELECT SUM(durationMinutes) FROM deep_work_records WHERE date = :date")
    fun getTotalMinutesForDate(date: String): Flow<Int?>
}
