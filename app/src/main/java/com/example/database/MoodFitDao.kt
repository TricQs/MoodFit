package com.example.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MoodFitDao {
    @Query("SELECT * FROM saved_activities ORDER BY timestamp DESC")
    fun getAllSavedActivities(): Flow<List<SavedActivity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedActivity(activity: SavedActivity)

    @Query("DELETE FROM saved_activities WHERE id = :id")
    suspend fun deleteSavedActivityById(id: Int)

    @Query("SELECT EXISTS(SELECT 1 FROM saved_activities WHERE title = :title LIMIT 1)")
    suspend fun isActivitySaved(title: String): Boolean

    @Query("DELETE FROM saved_activities WHERE title = :title")
    suspend fun deleteSavedActivityByTitle(title: String)

    // Mood Log Queries for Analytics
    @Query("SELECT * FROM mood_logs ORDER BY timestamp DESC")
    fun getAllMoodLogs(): Flow<List<MoodLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMoodLog(log: MoodLog)

    @Query("DELETE FROM mood_logs")
    suspend fun clearMoodLogs()
}
