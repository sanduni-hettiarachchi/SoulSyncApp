package com.wellnessapp.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

/**
 * Centralized data management class for the SoulSync app
 * Handles all data persistence using SharedPreferences and provides real-time data access
 * 
 * Features:
 * - Habit management (CRUD operations)
 * - Mood logging and retrieval
 * - Hydration tracking
 * - Settings persistence
 * - Real-time data synchronization
 */
class DataManager private constructor(context: Context) {

    companion object {
        @Volatile
        private var INSTANCE: DataManager? = null

        fun getInstance(context: Context): DataManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: DataManager(context.applicationContext).also { INSTANCE = it }
            }
        }

        // SharedPreferences keys
        private const val PREFS_NAME = "soulsync_prefs"
        private const val KEY_HABITS = "habits"
        private const val KEY_MOODS = "moods"
        private const val KEY_HYDRATION_GOAL = "hydration_goal"
        private const val KEY_HYDRATION_CURRENT = "hydration_current"
        private const val KEY_HYDRATION_LAST_UPDATE = "hydration_last_update"
        private const val KEY_NOTIFICATION_ENABLED = "notification_enabled"
        private const val KEY_NOTIFICATION_INTERVAL = "notification_interval"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_DAILY_GOALS = "daily_goals"
    }

    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    // Real-time data listeners
    private val habitListeners = mutableListOf<(List<Habit>) -> Unit>()
    private val moodListeners = mutableListOf<(List<Mood>) -> Unit>()
    private val hydrationListeners = mutableListOf<(Int, Int) -> Unit>() // current, goal

    /**
     * HABIT MANAGEMENT
     */

    /**
     * Saves a habit to persistent storage
     * @param habit The habit to save
     */
    fun saveHabit(habit: Habit) {
        val habits = getHabits().toMutableList()
        val existingIndex = habits.indexOfFirst { it.id == habit.id }
        
        if (existingIndex >= 0) {
            habits[existingIndex] = habit
        } else {
            habits.add(habit)
        }
        
        val habitsJson = gson.toJson(habits)
        sharedPreferences.edit().putString(KEY_HABITS, habitsJson).apply()
        
        // Notify listeners
        notifyHabitListeners(habits)
    }

    /**
     * Retrieves all habits from storage
     * @return List of all saved habits
     */
    fun getHabits(): List<Habit> {
        val habitsJson = sharedPreferences.getString(KEY_HABITS, null) ?: return emptyList()
        val type = object : TypeToken<List<Habit>>() {}.type
        return gson.fromJson(habitsJson, type) ?: emptyList()
    }

    /**
     * Deletes a habit by ID
     * @param habitId The ID of the habit to delete
     */
    fun deleteHabit(habitId: String) {
        val habits = getHabits().toMutableList()
        habits.removeAll { it.id == habitId }
        
        val habitsJson = gson.toJson(habits)
        sharedPreferences.edit().putString(KEY_HABITS, habitsJson).apply()
        
        // Notify listeners
        notifyHabitListeners(habits)
    }

    /**
     * MOOD MANAGEMENT
     */

    /**
     * Saves a mood entry to storage
     * @param mood The mood to save
     */
    fun saveMood(mood: Mood) {
        val moods = getMoods().toMutableList()
        moods.add(mood)
        
        // Keep only last 365 days of moods
        val oneYearAgo = System.currentTimeMillis() - (365 * 24 * 60 * 60 * 1000L)
        val filteredMoods = moods.filter { it.date >= oneYearAgo }
        
        val moodsJson = gson.toJson(filteredMoods)
        sharedPreferences.edit().putString(KEY_MOODS, moodsJson).apply()
        
        // Notify listeners
        notifyMoodListeners(filteredMoods)
    }

    /**
     * Retrieves all moods from storage
     * @return List of all saved moods
     */
    fun getMoods(): List<Mood> {
        val moodsJson = sharedPreferences.getString(KEY_MOODS, null) ?: return emptyList()
        val type = object : TypeToken<List<Mood>>() {}.type
        return gson.fromJson(moodsJson, type) ?: emptyList()
    }

    /**
     * Gets moods for a specific date range
     * @param startDate Start date in milliseconds
     * @param endDate End date in milliseconds
     * @return List of moods within the date range
     */
    fun getMoodsInRange(startDate: Long, endDate: Long): List<Mood> {
        return getMoods().filter { it.date in startDate..endDate }
    }

    /**
     * HYDRATION MANAGEMENT
     */

    /**
     * Sets the daily hydration goal
     * @param goal Number of glasses per day
     */
    fun setHydrationGoal(goal: Int) {
        sharedPreferences.edit().putInt(KEY_HYDRATION_GOAL, goal).apply()
        val current = getCurrentHydration()
        notifyHydrationListeners(current, goal)
    }

    /**
     * Gets the daily hydration goal
     * @return Number of glasses per day
     */
    fun getHydrationGoal(): Int {
        return sharedPreferences.getInt(KEY_HYDRATION_GOAL, 8)
    }

    /**
     * Sets whether hydration reminders are enabled
     * @param enabled True to enable reminders
     */
    fun setReminderEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_NOTIFICATION_ENABLED, enabled).apply()
    }

    /**
     * Gets whether hydration reminders are enabled
     * @return True if reminders are enabled
     */
    fun getReminderEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_NOTIFICATION_ENABLED, true)
    }

    /**
     * Adds water intake
     * @param glasses Number of glasses to add
     */
    fun addWaterIntake(glasses: Int) {
        val current = getCurrentHydration()
        val newCurrent = current + glasses
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        sharedPreferences.edit()
            .putInt(KEY_HYDRATION_CURRENT, newCurrent)
            .putLong(KEY_HYDRATION_LAST_UPDATE, today)
            .apply()

        notifyHydrationListeners(newCurrent, getHydrationGoal())
    }

    /**
     * Gets current hydration for today
     * @return Number of glasses consumed today
     */
    fun getCurrentHydration(): Int {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val lastUpdate = sharedPreferences.getLong(KEY_HYDRATION_LAST_UPDATE, 0)
        
        // Reset if it's a new day
        return if (lastUpdate < today) {
            sharedPreferences.edit()
                .putInt(KEY_HYDRATION_CURRENT, 0)
                .putLong(KEY_HYDRATION_LAST_UPDATE, today)
                .apply()
            0
        } else {
            sharedPreferences.getInt(KEY_HYDRATION_CURRENT, 0)
        }
    }

    /**
     * SETTINGS MANAGEMENT
     */

    /**
     * Sets notification preferences
     * @param enabled Whether notifications are enabled
     * @param intervalHours Interval between notifications in hours
     */
    fun setNotificationSettings(enabled: Boolean, intervalHours: Int) {
        sharedPreferences.edit()
            .putBoolean(KEY_NOTIFICATION_ENABLED, enabled)
            .putInt(KEY_NOTIFICATION_INTERVAL, intervalHours)
            .apply()
    }

    /**
     * Gets notification settings
     * @return Pair of (enabled, intervalHours)
     */
    fun getNotificationSettings(): Pair<Boolean, Int> {
        return Pair(
            sharedPreferences.getBoolean(KEY_NOTIFICATION_ENABLED, true),
            sharedPreferences.getInt(KEY_NOTIFICATION_INTERVAL, 2)
        )
    }

    /**
     * Sets user name
     * @param name User's name
     */
    fun setUserName(name: String) {
        sharedPreferences.edit().putString(KEY_USER_NAME, name).apply()
    }

    /**
     * Gets user name
     * @return User's name or empty string if not set
     */
    fun getUserName(): String {
        return sharedPreferences.getString(KEY_USER_NAME, "") ?: ""
    }

    /**
     * REAL-TIME DATA LISTENERS
     */

    /**
     * Adds a listener for habit changes
     * @param listener Function to call when habits change
     */
    fun addHabitListener(listener: (List<Habit>) -> Unit) {
        habitListeners.add(listener)
        // Immediately call with current data
        listener(getHabits())
    }

    /**
     * Removes a habit listener
     * @param listener The listener to remove
     */
    fun removeHabitListener(listener: (List<Habit>) -> Unit) {
        habitListeners.remove(listener)
    }

    /**
     * Adds a listener for mood changes
     * @param listener Function to call when moods change
     */
    fun addMoodListener(listener: (List<Mood>) -> Unit) {
        moodListeners.add(listener)
        // Immediately call with current data
        listener(getMoods())
    }

    /**
     * Removes a mood listener
     * @param listener The listener to remove
     */
    fun removeMoodListener(listener: (List<Mood>) -> Unit) {
        moodListeners.remove(listener)
    }

    /**
     * Adds a listener for hydration changes
     * @param listener Function to call when hydration changes (current, goal)
     */
    fun addHydrationListener(listener: (Int, Int) -> Unit) {
        hydrationListeners.add(listener)
        // Immediately call with current data
        listener(getCurrentHydration(), getHydrationGoal())
    }

    /**
     * Removes a hydration listener
     * @param listener The listener to remove
     */
    fun removeHydrationListener(listener: (Int, Int) -> Unit) {
        hydrationListeners.remove(listener)
    }

    /**
     * Notifies all habit listeners
     */
    private fun notifyHabitListeners(habits: List<Habit>) {
        habitListeners.forEach { it(habits) }
    }

    /**
     * Notifies all mood listeners
     */
    private fun notifyMoodListeners(moods: List<Mood>) {
        moodListeners.forEach { it(moods) }
    }

    /**
     * Notifies all hydration listeners
     */
    private fun notifyHydrationListeners(current: Int, goal: Int) {
        hydrationListeners.forEach { it(current, goal) }
    }

    /**
     * UTILITY METHODS
     */

    /**
     * Clears all data (for testing or reset functionality)
     */
    fun clearAllData() {
        sharedPreferences.edit().clear().apply()
        notifyHabitListeners(emptyList())
        notifyMoodListeners(emptyList())
        notifyHydrationListeners(0, 8)
    }

    /**
     * Gets daily completion percentage
     * @return Percentage of daily goals completed (0-100)
     */
    fun getDailyCompletionPercentage(): Int {
        val habits = getHabits()
        val hydrationGoal = getHydrationGoal()
        val hydrationCurrent = getCurrentHydration()
        
        if (habits.isEmpty() && hydrationGoal == 0) return 0
        
        var completedTasks = 0
        var totalTasks = 0
        
        // Count habit completions (simplified - assumes all habits are daily)
        habits.forEach { habit ->
            totalTasks++
            // This would need to be implemented based on your habit completion logic
            // For now, we'll use a placeholder
            if (habit.isActive) completedTasks++
        }
        
        // Add hydration progress
        if (hydrationGoal > 0) {
            totalTasks++
            if (hydrationCurrent >= hydrationGoal) {
                completedTasks++
            }
        }
        
        return if (totalTasks > 0) (completedTasks * 100) / totalTasks else 0
    }

    // Legacy method names for compatibility
    fun getHydration(): Int = getCurrentHydration()
    fun getHabitCompletions(): List<Habit> = getHabits()
    fun addMood(mood: Mood) = saveMood(mood)
    fun saveHydration(glasses: Int) = addWaterIntake(glasses)
    fun markHabitComplete(habitId: String) {
        // Implementation for marking habit as complete
        // This would need to be implemented based on your habit completion logic
    }
    fun markHabitIncomplete(habitId: String) {
        // Implementation for marking habit as incomplete
    }
    fun getHabitCompletionForDate(date: Long): List<Habit> = getHabits()
    fun getMoodsForDate(date: Long): List<Mood> = getMoods().filter { 
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = it.date
        val moodDate = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        val targetDate = Calendar.getInstance().apply {
            timeInMillis = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        moodDate == targetDate
    }
    fun getMoodStats(): Map<String, Int> {
        val moods = getMoods()
        return moods.groupBy { it.id }.mapValues { it.value.size }
    }
    fun getMoodsForWeek(startDate: Long): List<Mood> {
        val endDate = startDate + (7 * 24 * 60 * 60 * 1000L)
        return getMoodsInRange(startDate, endDate)
    }
    fun getHabitsCompletedForDate(date: Long): List<Habit> = getHabits()
    fun saveHabits(habits: List<Habit>) {
        habits.forEach { saveHabit(it) }
    }
    
    fun getMoodsForWeek(startDate: Long, endDate: Long): List<Mood> {
        return getMoodsInRange(startDate, endDate)
    }
    
    fun getHydrationForDate(date: Long): Int {
        val dateKey = "hydration_$date"
        return sharedPreferences.getInt(dateKey, 0)
    }
    
    fun getHydrationForWeek(startDate: Long, endDate: Long): Int {
        var totalGlasses = 0
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = startDate
        
        while (calendar.timeInMillis <= endDate) {
            val dateKey = "hydration_${calendar.timeInMillis}"
            totalGlasses += sharedPreferences.getInt(dateKey, 0)
            calendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        
        return totalGlasses
    }
    
    /**
     * Get moods for a specific month
     */
    fun getMoodsForMonth(year: Int, month: Int): List<Mood> {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.timeInMillis
        
        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.MILLISECOND, -1)
        val endOfMonth = calendar.timeInMillis
        
        return getMoodsForDateRange(startOfMonth, endOfMonth)
    }
    
    /**
     * Get hydration for a specific month
     */
    fun getHydrationForMonth(year: Int, month: Int): Int {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1, 0, 0, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.timeInMillis
        
        calendar.add(Calendar.MONTH, 1)
        calendar.add(Calendar.MILLISECOND, -1)
        val endOfMonth = calendar.timeInMillis
        
        var totalGlasses = 0
        val tempCalendar = Calendar.getInstance()
        tempCalendar.timeInMillis = startOfMonth
        
        while (tempCalendar.timeInMillis <= endOfMonth) {
            val dateKey = "hydration_${tempCalendar.timeInMillis}"
            totalGlasses += sharedPreferences.getInt(dateKey, 0)
            tempCalendar.add(Calendar.DAY_OF_MONTH, 1)
        }
        
        return totalGlasses
    }
    
    /**
     * Get moods for a date range
     */
    private fun getMoodsForDateRange(startDate: Long, endDate: Long): List<Mood> {
        val moods = getMoods()
        return moods.filter { it.date in startDate..endDate }
    }
    
    /**
     * Generate dummy data for 5 months
     */
    fun generateDummyData() {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        
        // Generate data for the last 5 months
        for (monthOffset in 4 downTo 0) {
            val targetMonth = (currentMonth - monthOffset + 12) % 12
            val targetYear = if (currentMonth - monthOffset < 0) currentYear - 1 else currentYear
            
            generateDummyMoodsForMonth(targetYear, targetMonth)
            generateDummyHydrationForMonth(targetYear, targetMonth)
        }
    }
    
    /**
     * Generate dummy mood data for a specific month
     */
    private fun generateDummyMoodsForMonth(year: Int, month: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        val moodTypes = listOf("happy", "neutral", "sad", "excited", "tired")
        val moodEmojis = listOf("😄", "😐", "😢", "🤩", "😴")
        
        for (day in 1..daysInMonth) {
            // Generate 1-3 moods per day
            val moodsPerDay = (1..3).random()
            for (moodIndex in 0 until moodsPerDay) {
                val moodTypeIndex = (0 until moodTypes.size).random()
                val mood = Mood(
                    id = "dummy_mood_${year}_${month}_${day}_${moodIndex}",
                    emoji = moodEmojis[moodTypeIndex],
                    date = calendar.timeInMillis + (day - 1) * 24 * 60 * 60 * 1000L + moodIndex * 4 * 60 * 60 * 1000L
                )
                saveMood(mood)
            }
        }
    }
    
    /**
     * Generate dummy hydration data for a specific month
     */
    private fun generateDummyHydrationForMonth(year: Int, month: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        
        for (day in 1..daysInMonth) {
            // Generate 3-8 glasses per day
            val glasses = (3..8).random()
            val date = calendar.timeInMillis + (day - 1) * 24 * 60 * 60 * 1000L
            
            val dateKey = "hydration_$date"
            sharedPreferences.edit().putInt(dateKey, glasses).apply()
        }
    }
    
    /**
     * Save daily step count
     */
    fun saveSteps(steps: Int) {
        val today = java.util.Calendar.getInstance()
        val dateKey = "${today.get(java.util.Calendar.YEAR)}-${today.get(java.util.Calendar.MONTH) + 1}-${today.get(java.util.Calendar.DAY_OF_MONTH)}"
        
        sharedPreferences.edit()
            .putInt("steps_$dateKey", steps)
            .apply()
    }
    
    /**
     * Get current step count for today
     */
    fun getSteps(): Int {
        val today = java.util.Calendar.getInstance()
        val dateKey = "${today.get(java.util.Calendar.YEAR)}-${today.get(java.util.Calendar.MONTH) + 1}-${today.get(java.util.Calendar.DAY_OF_MONTH)}"
        
        return sharedPreferences.getInt("steps_$dateKey", 0)
    }
    
    /**
     * Get step goal (default 10000)
     */
    fun getStepGoal(): Int {
        return sharedPreferences.getInt("step_goal", 10000)
    }
    
    /**
     * Set step goal
     */
    fun setStepGoal(goal: Int) {
        sharedPreferences.edit()
            .putInt("step_goal", goal)
            .apply()
    }
    
    /**
     * Get glass completion status for a specific glass number
     */
    fun getGlassCompletion(glassNumber: Int): Boolean {
        val today = java.util.Calendar.getInstance()
        val dateKey = "${today.get(java.util.Calendar.YEAR)}-${today.get(java.util.Calendar.MONTH) + 1}-${today.get(java.util.Calendar.DAY_OF_MONTH)}"
        return sharedPreferences.getBoolean("glass_${glassNumber}_$dateKey", false)
    }
    
    /**
     * Set glass completion status for a specific glass number
     */
    fun setGlassCompletion(glassNumber: Int, completed: Boolean) {
        val today = java.util.Calendar.getInstance()
        val dateKey = "${today.get(java.util.Calendar.YEAR)}-${today.get(java.util.Calendar.MONTH) + 1}-${today.get(java.util.Calendar.DAY_OF_MONTH)}"
        sharedPreferences.edit()
            .putBoolean("glass_${glassNumber}_$dateKey", completed)
            .apply()
    }
    
    /**
     * Get reminder interval in hours (default 2)
     */
    fun getReminderInterval(): Int {
        return sharedPreferences.getInt("reminder_interval", 2)
    }
    
    /**
     * Set reminder interval in hours
     */
    fun setReminderInterval(interval: Int) {
        sharedPreferences.edit()
            .putInt("reminder_interval", interval)
            .apply()
    }
    
    
    /**
     * Get moods for today
     */
    fun getMoodsForToday(): List<Mood> {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        
        val tomorrow = today + (24 * 60 * 60 * 1000)
        
        val moodsJson = sharedPreferences.getString("moods", "[]") ?: "[]"
        val allMoods = try {
            gson.fromJson(moodsJson, Array<Mood>::class.java).toList()
        } catch (e: Exception) {
            emptyList()
        }
        
        return allMoods.filter { mood ->
            mood.timestamp >= today && mood.timestamp < tomorrow
        }
    }
}