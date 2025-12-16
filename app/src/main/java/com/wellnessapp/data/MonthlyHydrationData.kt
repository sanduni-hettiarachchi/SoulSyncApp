package com.wellnessapp.data

/**
 * Data class for monthly hydration data
 * Contains daily hydration data for a month
 */
data class MonthlyHydrationData(
    val date: Long, // Timestamp of the day
    val glasses: Int, // Number of glasses consumed
    val liters: Float, // Equivalent in liters
    val goal: Int, // Daily goal in glasses
    val percentage: Float, // Completion percentage
    val isGoalMet: Boolean // Whether daily goal was met
)
