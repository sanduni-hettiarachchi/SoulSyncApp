package com.wellnessapp.data

/**
 * Data class representing hydration tracking
 * @param glassesDrank Number of glasses of water consumed
 * @param dailyGoal Daily goal for water intake
 * @param reminderInterval Interval between reminders in minutes
 * @param remindersEnabled Whether reminders are enabled
 */
data class Hydration(
    val glassesDrank: Int = 0,
    val dailyGoal: Int = 8,
    val reminderInterval: Int = 60,
    val remindersEnabled: Boolean = true
)

/**
 * Data class representing a hydration entry
 * @param id Unique identifier
 * @param amount Amount of water in liters
 * @param timestamp When the water was consumed
 * @param notes Optional notes
 */
data class HydrationEntry(
    val id: String = "",
    val amount: Double = 0.25, // 250ml = 0.25L
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)