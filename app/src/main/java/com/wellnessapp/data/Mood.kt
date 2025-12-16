package com.wellnessapp.data

import java.io.Serializable
import java.util.Date

/**
 * Data class representing a mood entry
 * @param id Unique identifier for the mood entry
 * @param emoji Emoji representing the mood
 * @param note Optional note about the mood
 * @param date Date and time of the mood entry
 * @param color Color associated with the mood
 */
data class Mood(
    val id: String = "",
    val emoji: String = "",
    val type: String = "neutral",
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = "",
    val date: Long = System.currentTimeMillis(),
    val color: String = ""
) : Serializable {
    /**
     * Get the mood level based on emoji (1-10 scale)
     */
    fun getMoodLevel(): Int {
        return when (emoji) {
            "😀", "🤩", "😍" -> 10
            "😊", "😌", "😄" -> 8
            "😐", "😑" -> 5
            "😔", "😕" -> 3
            "😢", "😰", "🤒" -> 1
            else -> 5
        }
    }
    
    /**
     * Get the mood description
     */
    fun getMoodDescription(): String {
        return when (emoji) {
            "😀" -> "Happy"
            "😢" -> "Sad"
            "😠" -> "Angry"
            "🤩" -> "Excited"
            "😴" -> "Tired"
            "😰" -> "Stressed"
            "😌" -> "Calm"
            "😍" -> "In Love"
            "😐" -> "Neutral"
            "🤒" -> "Sick"
            else -> "Unknown"
        }
    }
}

/**
 * Predefined mood types with their properties
 */
object MoodTypes {
    val HAPPY = Mood(
        id = "happy",
        emoji = "😄",
        color = "mood_happy"
    )
    
    val SAD = Mood(
        id = "sad",
        emoji = "😢",
        color = "mood_sad"
    )
    
    val ANGRY = Mood(
        id = "angry",
        emoji = "😡",
        color = "mood_angry"
    )
    
    val EXCITED = Mood(
        id = "excited",
        emoji = "🤩",
        color = "mood_excited"
    )
    
    val TIRED = Mood(
        id = "tired",
        emoji = "😴",
        color = "mood_tired"
    )
    
    val STRESSED = Mood(
        id = "stressed",
        emoji = "😰",
        color = "mood_stressed"
    )
    
    val CALM = Mood(
        id = "calm",
        emoji = "😌",
        color = "mood_calm"
    )
    
    val LOVE = Mood(
        id = "love",
        emoji = "🥰",
        color = "mood_love"
    )
    
    val NEUTRAL = Mood(
        id = "neutral",
        emoji = "😐",
        color = "mood_neutral"
    )
    
    val SICK = Mood(
        id = "sick",
        emoji = "🤒",
        color = "mood_sick"
    )
    
    val CONFUSED = Mood(
        id = "confused",
        emoji = "😕",
        color = "mood_confused"
    )
    
    val SURPRISED = Mood(
        id = "surprised",
        emoji = "😲",
        color = "mood_surprised"
    )
    
    val WORRIED = Mood(
        id = "worried",
        emoji = "😟",
        color = "mood_worried"
    )
    
    val GRATEFUL = Mood(
        id = "grateful",
        emoji = "🙏",
        color = "mood_grateful"
    )
    
    val PROUD = Mood(
        id = "proud",
        emoji = "😎",
        color = "mood_proud"
    )
    
    val NERVOUS = Mood(
        id = "nervous",
        emoji = "😬",
        color = "mood_nervous"
    )
    
    val CONTENT = Mood(
        id = "content",
        emoji = "😊",
        color = "mood_content"
    )
    
    val FRUSTRATED = Mood(
        id = "frustrated",
        emoji = "😤",
        color = "mood_frustrated"
    )
    
    val HOPEFUL = Mood(
        id = "hopeful",
        emoji = "🤞",
        color = "mood_hopeful"
    )
    
    val PEACEFUL = Mood(
        id = "peaceful",
        emoji = "🧘‍♀️",
        color = "mood_peaceful"
    )
    
    /**
     * Get all available mood types
     */
    fun getAllMoods(): List<Mood> {
        return listOf(
            HAPPY, SAD, ANGRY, EXCITED, TIRED,
            STRESSED, CALM, LOVE, NEUTRAL, SICK,
            CONFUSED, SURPRISED, WORRIED, GRATEFUL,
            PROUD, NERVOUS, CONTENT, FRUSTRATED,
            HOPEFUL, PEACEFUL
        )
    }
}

/**
 * Data class representing mood statistics for a period
 * @param averageMood Average mood level for the period
 * @param totalEntries Number of mood entries
 * @param mostCommonMood Most frequently selected mood
 * @param moodTrend Trend direction (up, down, stable)
 */
data class MoodStats(
    val averageMood: Float = 0f,
    val totalEntries: Int = 0,
    val mostCommonMood: String = "",
    val moodTrend: String = "stable"
)
