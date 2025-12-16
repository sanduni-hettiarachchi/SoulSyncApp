package com.wellnessapp.data

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.wellnessapp.data.Habit
import com.wellnessapp.data.Mood
import com.wellnessapp.data.WeeklyStats
import com.wellnessapp.data.MonthlyStats
import com.wellnessapp.data.MonthlyHydrationData
import java.util.*

/**
 * Real-time data manager for the entire app
 * Handles live updates across all fragments and activities
 */
class RealtimeDataManager private constructor() {
    
    companion object {
        @Volatile
        private var INSTANCE: RealtimeDataManager? = null
        
        fun getInstance(): RealtimeDataManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: RealtimeDataManager().also { INSTANCE = it }
            }
        }
    }
    
    // Live data for real-time updates
    val todayHabits = MutableLiveData<List<Habit>>()
    val todayMoods = MutableLiveData<List<Mood>>()
    val weeklyStats = MutableLiveData<WeeklyStats>()
    val monthlyStats = MutableLiveData<MonthlyStats>()
    val hydrationProgress = MutableLiveData<Int>() // glasses of water
    val habitCompletionPercentage = MutableLiveData<Int>()
    val stepCount = MutableLiveData<Int>()
    val currentMood = MutableLiveData<Mood>()
    val habitCompletion = MutableLiveData<Int>()
    val hydrationLevel = MutableLiveData<Int>()
    val habitProgress = MutableLiveData<Int>() // Real-time habit progress updates
    
    // Monthly data for charts
    val monthlyMoods = MutableLiveData<List<Mood>>()
    val monthlyHydration = MutableLiveData<List<MonthlyHydrationData>>()
    
    private var dataManager: DataManager? = null
    private var isInitialized = false
    
    fun initialize(context: Context) {
        // Prevent multiple initializations
        if (isInitialized) {
            android.util.Log.d("RealtimeDataManager", "Already initialized, skipping")
            return
        }
        
        dataManager = DataManager.getInstance(context)
        isInitialized = true
        
        // Load data asynchronously to avoid blocking UI
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            loadInitialData()
        }
        android.util.Log.d("RealtimeDataManager", "Initialized with data manager: $dataManager")
    }
    
    private fun loadInitialData() {
        dataManager?.let { dm ->
            android.util.Log.d("RealtimeDataManager", "Loading initial data...")
            
            // Load today's habits
            val habits = dm.getHabits()
            todayHabits.value = habits
            android.util.Log.d("RealtimeDataManager", "Loaded ${habits.size} habits")
            
            // Calculate completion percentage
            val completedHabits = habits.count { it.isActive }
            val totalHabits = habits.size
            val percentage = if (totalHabits > 0) (completedHabits * 100) / totalHabits else 0
            habitCompletionPercentage.value = percentage
            habitCompletion.value = percentage
            android.util.Log.d("RealtimeDataManager", "Habit completion: $completedHabits/$totalHabits ($percentage%)")
            
            // Load today's moods
            val today = Calendar.getInstance()
            val moods = dm.getMoodsForDate(today.timeInMillis)
            todayMoods.value = moods
            android.util.Log.d("RealtimeDataManager", "Loaded ${moods.size} moods for today")
            
            // Load weekly stats
            loadWeeklyStats()
            
            // Load hydration data
            val hydration = dm.getHydrationForDate(today.timeInMillis)
            hydrationProgress.value = hydration
            hydrationLevel.value = hydration
            android.util.Log.d("RealtimeDataManager", "Loaded hydration: $hydration glasses")
            
            // Initialize step count (would come from sensor in real app)
            stepCount.value = 5489
            android.util.Log.d("RealtimeDataManager", "Initial data loaded successfully")
        } ?: run {
            android.util.Log.e("RealtimeDataManager", "DataManager is null - cannot load initial data")
        }
    }
    
    fun updateHabitCompletion(habit: Habit) {
        dataManager?.saveHabit(habit)
        loadInitialData() // Refresh all data
    }
    
    fun updateHabitCompletionPercentage(percentage: Int) {
        habitCompletionPercentage.value = percentage
        habitCompletion.value = percentage
        android.util.Log.d("RealtimeDataManager", "Updated habit completion percentage: $percentage%")
    }
    
    fun addHabit(habit: Habit) {
        dataManager?.saveHabit(habit)
        loadInitialData()
    }
    
    fun deleteHabit(habitId: String) {
        dataManager?.deleteHabit(habitId)
        loadInitialData()
    }
    
    fun addMood(mood: Mood) {
        dataManager?.saveMood(mood)
        loadInitialData()
    }
    
    fun updateHydration(glasses: Int) {
        dataManager?.saveHydration(glasses)
        hydrationProgress.value = glasses
    }
    
    fun updateStepCount(steps: Int) {
        stepCount.value = steps
    }
    
    private fun loadWeeklyStats() {
        val calendar = Calendar.getInstance()
        val startOfWeek = calendar.apply {
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val endOfWeek = Calendar.getInstance().apply {
            time = startOfWeek.time
            add(Calendar.DAY_OF_WEEK, 6)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }
        
        dataManager?.let { dm ->
            val habits = dm.getHabits()
            val moods = dm.getMoodsForWeek(startOfWeek.timeInMillis, endOfWeek.timeInMillis)
            val hydration = dm.getHydrationForWeek(startOfWeek.timeInMillis, endOfWeek.timeInMillis)
            
            val stats = WeeklyStats(
                weekStart = startOfWeek.timeInMillis,
                weekEnd = endOfWeek.timeInMillis,
                totalHabits = habits.size,
                completedHabits = habits.count { it.isActive },
                totalMoods = moods.size,
                averageMood = if (moods.isNotEmpty()) moods.map { it.getMoodLevel() }.average().toFloat() else 0f,
                totalHydration = hydration,
                averageHydration = if (hydration > 0) hydration.toFloat() / 7 else 0f, // Average per day
                streakDays = 0, // TODO: Calculate streak
                bestDay = "Monday", // TODO: Calculate best day
                improvementRate = 0.0f // TODO: Calculate improvement rate
            )
            
            weeklyStats.value = stats
        }
    }
    
    fun refreshAllData() {
        loadInitialData()
    }
    
    // Additional methods for HomeFragment compatibility
    fun startRealtimeSimulation() {
        // Start any real-time simulation if needed
    }
    
    fun stopRealtimeSimulation() {
        // Stop any real-time simulation if needed
    }
    
    fun getHabitCompletionValue(): Int {
        return habitCompletionPercentage.value ?: 0
    }
    
    fun completeHabit(habit: Habit) {
        updateHabitCompletion(habit)
    }
    
    fun setDataManager(dataManager: DataManager) {
        this.dataManager = dataManager
    }
    
    fun refreshData() {
        loadInitialData()
    }
    
    fun updateHydrationLevel(level: Int) {
        hydrationLevel.value = level
    }
    
    fun getDataManager(): DataManager? {
        return dataManager
    }
    
    /**
     * Load monthly data for charts
     */
    fun loadMonthlyData() {
        dataManager?.let { dm ->
            val calendar = Calendar.getInstance()
            val currentYear = calendar.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH)
            
            // Load moods for the last 5 months
            val monthlyMoodsList = mutableListOf<Mood>()
            val monthlyHydrationList = mutableListOf<MonthlyHydrationData>()
            
            for (monthOffset in 4 downTo 0) {
                val targetMonth = (currentMonth - monthOffset + 12) % 12
                val targetYear = if (currentMonth - monthOffset < 0) currentYear - 1 else currentYear
                
                // Load moods for this month
                val monthMoods = dm.getMoodsForMonth(targetYear, targetMonth)
                monthlyMoodsList.addAll(monthMoods)
                
                // Load hydration for this month
                val monthHydration = dm.getHydrationForMonth(targetYear, targetMonth)
                val daysInMonth = calendar.apply { set(targetYear, targetMonth, 1) }.getActualMaximum(Calendar.DAY_OF_MONTH)
                val averageGlasses = if (daysInMonth > 0) monthHydration.toFloat() / daysInMonth else 0f
                
                monthlyHydrationList.add(
                    MonthlyHydrationData(
                        date = calendar.timeInMillis,
                        glasses = monthHydration,
                        liters = monthHydration * 0.25f, // Convert glasses to liters
                        goal = 8, // Default goal
                        percentage = if (monthHydration > 0) (monthHydration.toFloat() / 8 * 100) else 0f,
                        isGoalMet = monthHydration >= 8
                    )
                )
            }
            
            monthlyMoods.value = monthlyMoodsList
            monthlyHydration.value = monthlyHydrationList
            
            // Calculate monthly stats
            val habits = dm.getHabits()
            val completedHabits = habits.count { it.isActive }
            val totalHabits = habits.size
            val moodsLogged = monthlyMoodsList.size
            val waterGlasses = monthlyHydrationList.sumOf { it.glasses }
            val averageMood = if (monthlyMoodsList.isNotEmpty()) {
                monthlyMoodsList.map { it.getMoodLevel() }.average().toFloat()
            } else 0f
            
            val monthStartCalendar = Calendar.getInstance()
            val monthStart = monthStartCalendar.apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
            
            val monthEndCalendar = Calendar.getInstance()
            val monthEnd = monthEndCalendar.apply {
                set(Calendar.DAY_OF_MONTH, monthEndCalendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis
            
            val monthlyStatsData = MonthlyStats(
                monthStart = monthStart,
                monthEnd = monthEnd,
                totalHabits = totalHabits,
                completedHabits = completedHabits,
                totalMoods = moodsLogged,
                averageMood = averageMood,
                totalHydration = waterGlasses,
                averageHydration = if (monthlyHydrationList.isNotEmpty()) monthlyHydrationList.map { it.glasses }.average().toFloat() else 0f,
                streakDays = 0, // TODO: Calculate streak
                bestWeek = "Week 1", // TODO: Calculate best week
                improvementRate = 0.0f, // TODO: Calculate improvement
                weeklyBreakdown = emptyList() // TODO: Add weekly breakdown
            )
            
            monthlyStats.value = monthlyStatsData
            
            android.util.Log.d("RealtimeDataManager", "Monthly data loaded: ${monthlyMoodsList.size} moods, ${monthlyHydrationList.size} months")
        }
    }
    
    /**
     * Generate dummy data if needed
     */
    fun generateDummyDataIfNeeded() {
        dataManager?.let { dm ->
            // Check if we have any data, if not generate dummy data
            val existingMoods = dm.getMoods()
            if (existingMoods.isEmpty()) {
                android.util.Log.d("RealtimeDataManager", "No existing data found, generating dummy data")
                dm.generateDummyData()
                loadInitialData()
                loadMonthlyData()
            }
        }
    }
    
    /**
     * Refresh habit progress and notify observers
     */
    fun refreshHabitProgress() {
        dataManager?.let { dm ->
            val habits = dm.getHabits()
            val completedToday = habits.count { habit ->
                habit.isCompletedForDate(Date())
            }
            val totalHabits = habits.size
            val progressPercentage = if (totalHabits > 0) (completedToday * 100) / totalHabits else 0
            
            // Update all habit-related LiveData
            habitProgress.value = progressPercentage
            habitCompletion.value = completedToday
            habitCompletionPercentage.value = progressPercentage
            todayHabits.value = habits
            
            android.util.Log.d("RealtimeDataManager", "Habit progress refreshed: $completedToday/$totalHabits ($progressPercentage%)")
        }
    }
}
