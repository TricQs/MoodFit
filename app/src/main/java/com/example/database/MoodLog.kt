package com.example.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mood_logs")
data class MoodLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val mood: String, // Happy, Stressed, Tired, Productive, Bored
    val timestamp: Long = System.currentTimeMillis(),
    val energyLevel: Int = 5 // 1 to 10
)
