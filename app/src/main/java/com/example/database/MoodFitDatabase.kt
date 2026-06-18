package com.example.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SavedActivity::class, MoodLog::class], version = 1, exportSchema = false)
abstract class MoodFitDatabase : RoomDatabase() {
    abstract fun moodFitDao(): MoodFitDao

    companion object {
        @Volatile
        private var INSTANCE: MoodFitDatabase? = null

        fun getDatabase(context: Context): MoodFitDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MoodFitDatabase::class.java,
                    "moodfit_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
