package com.wellnessapp.ui.stats

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wellnessapp.data.DataManager
import com.wellnessapp.data.WeeklyStats
import java.util.*

/**
 * ViewModel for Stats Fragment
 * Manages statistics and analytics data
 */
class StatsViewModel : ViewModel() {
    
    private val _weeklyStats = MutableLiveData<WeeklyStats>()
    val weeklyStats: LiveData<WeeklyStats> = _weeklyStats
    
    private val _moodTrend = MutableLiveData<List<String>>()
    val moodTrend: LiveData<List<String>> = _moodTrend
    
    private lateinit var dataManager: DataManager
    
    fun setDataManager(dataManager: DataManager) {
        this.dataManager = dataManager
        loadStats()
    }
    
    private fun loadStats() {
        loadWeeklyStats()
        loadMoodTrend()
    }
    
    private fun loadWeeklyStats() {
        val startCalendar = Calendar.getInstance()
        val startOfWeek = startCalendar.apply {
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
        
        val endCalendar = Calendar.getInstance()
        val endOfWeek = endCalendar.apply {
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time
        
        val habits = dataManager.getHabits()
        val completions = dataManager.getHabitCompletions()
        
        val weeklyCompletions = completions.filter { completion ->
            val completionDate = Date(completion.createdAt)
            completionDate >= startOfWeek && completionDate <= endOfWeek
        }
        
        val completedCount = weeklyCompletions.count { it.isActive }
        val totalPossible = habits.size * 7 // 7 days in a week
        
        val weekStartCalendar = Calendar.getInstance()
        val weekStart = weekStartCalendar.apply {
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        val weekEndCalendar = Calendar.getInstance()
        val weekEnd = weekEndCalendar.apply {
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
        
        val stats = WeeklyStats(
            weekStart = weekStart,
            weekEnd = weekEnd,
            totalHabits = habits.size,
            completedHabits = completedCount,
            totalMoods = 0, // TODO: Calculate from mood data
            averageMood = 3.0f, // TODO: Calculate from mood data
            totalHydration = 0, // TODO: Calculate from hydration data
            averageHydration = 0.0f, // TODO: Calculate from hydration data
            streakDays = calculateStreak(),
            bestDay = "Monday", // TODO: Calculate best day
            improvementRate = 0.0f // TODO: Calculate improvement rate
        )
        
        _weeklyStats.value = stats
    }
    
    private fun loadMoodTrend() {
        val moods = dataManager.getMoods()
        val last7Days = moods.sortedByDescending { it.date }.take(7)
        val moodEmojis = last7Days.map { it.emoji }
        _moodTrend.value = moodEmojis
    }
    
    private fun calculateStreak(): Int {
        // Simplified streak calculation
        val completions = dataManager.getHabitCompletions()
        val today = Date()
        var streak = 0
        
        for (i in 0..6) {
            val checkDate = Calendar.getInstance().apply {
                time = today
                add(Calendar.DAY_OF_YEAR, -i)
            }.time
            
            val dayCompletions = completions.filter { completion ->
                val completionDate = Date(completion.createdAt)
                val cal1 = Calendar.getInstance().apply { time = completionDate }
                val cal2 = Calendar.getInstance().apply { time = checkDate }
                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
            }
            
            if (dayCompletions.any { it.isActive }) {
                streak++
            } else {
                break
            }
        }
        
        return streak
    }
    
    fun refreshStats() {
        loadStats()
    }
}
