package com.wellnessapp.data

/**
 * Data class for weekly statistics
 * Contains aggregated data for a 7-day period
 */
data class WeeklyStats(
    val weekStart: Long, // Timestamp of week start
    val weekEnd: Long,   // Timestamp of week end
    val totalHabits: Int,
    val completedHabits: Int,
    val totalMoods: Int,
    val averageMood: Float,
    val totalHydration: Int, // Total glasses of water
    val averageHydration: Float,
    val streakDays: Int,
    val bestDay: String, // Day with highest completion
    val improvementRate: Float // Percentage improvement from previous week
)
