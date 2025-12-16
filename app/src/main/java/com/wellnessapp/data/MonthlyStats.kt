package com.wellnessapp.data

/**
 * Data class for monthly statistics
 * Contains aggregated data for a 30-day period
 */
data class MonthlyStats(
    val monthStart: Long, // Timestamp of month start
    val monthEnd: Long,    // Timestamp of month end
    val totalHabits: Int,
    val completedHabits: Int,
    val totalMoods: Int,
    val averageMood: Float,
    val totalHydration: Int, // Total glasses of water
    val averageHydration: Float,
    val streakDays: Int,
    val bestWeek: String, // Week with highest completion
    val improvementRate: Float, // Percentage improvement from previous month
    val weeklyBreakdown: List<WeeklyStats> // Weekly stats within the month
)
