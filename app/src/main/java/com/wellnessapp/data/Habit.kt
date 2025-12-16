package com.wellnessapp.data

import java.io.Serializable
import java.util.Date

/**
 * Data class representing a daily habit
 * @param id Unique identifier for the habit
 * @param name Display name of the habit
 * @param description Optional description
 * @param target Target value (e.g., "8 glasses", "30 min", "5000 steps")
 * @param icon Icon resource ID for the habit
 * @param color Color resource ID for the habit
 * @param isActive Whether the habit is currently active
 * @param createdAt When the habit was created
 */
data class Habit(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val target: String = "",
    val icon: String = "",
    val color: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val type: String = "",
    val duration: Int = 30,
    val category: String = "General"
) : Serializable {
    /**
     * Check if habit is completed for a specific date
     */
    fun isCompletedForDate(date: Date): Boolean {
        // This will be implemented in the data manager
        return false
    }
    
    /**
     * Get completion percentage for a specific date
     */
    fun getCompletionPercentage(date: Date): Int {
        // This will be implemented in the data manager
        return 0
    }
}

/**
 * Data class representing habit completion for a specific date
 * @param habitId ID of the habit
 * @param date Date of completion
 * @param isCompleted Whether the habit was completed
 * @param progress Progress value (0-100)
 * @param notes Optional notes
 */
data class HabitCompletion(
    val habitId: String = "",
    val date: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
    val progress: Int = 0,
    val notes: String = ""
)

/**
 * Predefined habit types with their properties
 */
object HabitTypes {
    val WATER = Habit(
        id = "water",
        name = "Drink Water",
        description = "Stay hydrated throughout the day",
        target = "8 glasses",
        icon = "💧",
        color = "habit_water"
    )
    
    val EXERCISE = Habit(
        id = "exercise",
        name = "Exercise",
        description = "Physical activity for health",
        target = "30 min",
        icon = "💪",
        color = "habit_exercise"
    )
    
    val MEDITATION = Habit(
        id = "meditation",
        name = "Meditation",
        description = "Mindfulness and relaxation",
        target = "10 min",
        icon = "🧘",
        color = "habit_meditation"
    )
    
    val READING = Habit(
        id = "reading",
        name = "Reading",
        description = "Read books or articles",
        target = "30 min",
        icon = "📚",
        color = "habit_reading"
    )
    
    val SLEEP = Habit(
        id = "sleep",
        name = "Sleep",
        description = "Get adequate sleep",
        target = "8 hours",
        icon = "😴",
        color = "habit_sleep"
    )
    
    val STEPS = Habit(
        id = "steps",
        name = "Steps",
        description = "Walk and stay active",
        target = "5000 steps",
        icon = "🚶",
        color = "habit_steps"
    )
    
    val YOGA = Habit(
        id = "yoga",
        name = "Yoga",
        description = "Yoga practice",
        target = "20 min",
        icon = "🧘‍♀️",
        color = "habit_yoga"
    )
    
    val RUNNING = Habit(
        id = "running",
        name = "Running",
        description = "Running or jogging",
        target = "5 km",
        icon = "🏃",
        color = "habit_running"
    )
    
    val CYCLING = Habit(
        id = "cycling",
        name = "Cycling",
        description = "Bicycle riding",
        target = "10 km",
        icon = "🚴",
        color = "habit_cycling"
    )
    
    val SWIMMING = Habit(
        id = "swimming",
        name = "Swimming",
        description = "Swimming exercise",
        target = "30 min",
        icon = "🏊",
        color = "habit_swimming"
    )
    
    val GYM = Habit(
        id = "gym",
        name = "Gym",
        description = "Gym workout",
        target = "45 min",
        icon = "🏋️",
        color = "habit_gym"
    )
    
    val GAMING = Habit(
        id = "gaming",
        name = "Gaming",
        description = "Gaming for entertainment",
        target = "1 hour",
        icon = "🎮",
        color = "habit_gaming"
    )
    
    val MUSIC = Habit(
        id = "music",
        name = "Music",
        description = "Listen to music",
        target = "30 min",
        icon = "🎵",
        color = "habit_music"
    )
    
    val GARDENING = Habit(
        id = "gardening",
        name = "Gardening",
        description = "Garden maintenance",
        target = "30 min",
        icon = "🌱",
        color = "habit_gardening"
    )
    
    val WALKING = Habit(
        id = "walking",
        name = "Walking",
        description = "Walking exercise",
        target = "30 min",
        icon = "🚶‍♀️",
        color = "habit_walking"
    )
    
    /**
     * Get all available habit types
     */
    fun getAllHabits(): List<Habit> {
        return listOf(
            WATER, EXERCISE, MEDITATION, READING, SLEEP, STEPS,
            YOGA, RUNNING, CYCLING, SWIMMING, GYM, GAMING,
            MUSIC, GARDENING, WALKING
        )
    }
}
