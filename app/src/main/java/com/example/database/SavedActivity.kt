package com.example.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_activities")
data class SavedActivity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val mood: String,
    val duration: String,
    val type: String, // Mental, Physical, Social, Creative
    val category: String, // Mindfulness, Fitness, Recreation, Productivity
    val timestamp: Long = System.currentTimeMillis()
)
