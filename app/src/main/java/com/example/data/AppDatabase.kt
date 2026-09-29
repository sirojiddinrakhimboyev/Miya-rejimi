package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.ChatDao
import com.example.data.dao.DeepWorkDao
import com.example.data.dao.ReactionDao
import com.example.data.dao.RewardDao
import com.example.data.dao.TaskDao
import com.example.data.entity.ChatMessage
import com.example.data.entity.DailyTask
import com.example.data.entity.DeepWorkRecord
import com.example.data.entity.ReactionScore
import com.example.data.entity.RewardMilestone

@Database(
    entities = [
        DailyTask::class,
        ReactionScore::class,
        DeepWorkRecord::class,
        ChatMessage::class,
        RewardMilestone::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun reactionDao(): ReactionDao
    abstract fun deepWorkDao(): DeepWorkDao
    abstract fun chatDao(): ChatDao
    abstract fun rewardDao(): RewardDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "miya_rejimi_db"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
