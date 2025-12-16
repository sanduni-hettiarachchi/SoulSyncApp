package com.wellnessapp.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.MutableLiveData
import com.wellnessapp.data.DataManager
import com.wellnessapp.data.Habit
import com.wellnessapp.data.Mood
import com.wellnessapp.data.Hydration
import java.util.*
import kotlin.random.Random

class RealtimeDataService : Service() {
    
    companion object {
        val currentMood = MutableLiveData<Mood>()
        val todayHabits = MutableLiveData<List<Habit>>()
        val hydrationLevel = MutableLiveData<Double>()
        val habitCompletion = MutableLiveData<Int>()
        val moodTrend = MutableLiveData<List<Mood>>()
        val isServiceRunning = MutableLiveData<Boolean>()
    }
    
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var dataManager: DataManager
    private var isRunning = false
    
    private val updateRunnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                updateRealtimeData()
                handler.postDelayed(this, 5000) // Update every 5 seconds
            }
        }
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        dataManager = DataManager.getInstance(this)
        startRealtimeUpdates()
        return START_STICKY
    }
    
    override fun onDestroy() {
        stopRealtimeUpdates()
        super.onDestroy()
    }
    
    private fun startRealtimeUpdates() {
        isRunning = true
        isServiceRunning.postValue(true)
        handler.post(updateRunnable)
    }
    
    private fun stopRealtimeUpdates() {
        isRunning = false
        isServiceRunning.postValue(false)
        handler.removeCallbacks(updateRunnable)
    }
    
    private fun updateRealtimeData() {
        // Update current mood
        updateCurrentMood()
        
        // Update today's habits
        updateTodayHabits()
        
        // Update hydration level
        updateHydrationLevel()
        
        // Update habit completion percentage
        updateHabitCompletion()
        
        // Update mood trend
        updateMoodTrend()
    }
    
    private fun updateCurrentMood() {
        val moods = dataManager.getMoods()
        if (moods.isNotEmpty()) {
            val latestMood = moods.maxByOrNull { it.date }
            currentMood.postValue(latestMood)
        } else {
            // Generate a sample mood if none exists
            val sampleMood = Mood(
                id = UUID.randomUUID().toString(),
                emoji = "😊",
                notes = "Happy",
                date = System.currentTimeMillis(),
                color = "mood_happy"
            )
            currentMood.postValue(sampleMood)
        }
    }
    
    private fun updateTodayHabits() {
        val habits = dataManager.getHabits()
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)
        
        val todayHabitsList = habits.filter { habit ->
            val habitDate = Calendar.getInstance()
            habitDate.timeInMillis = habit.createdAt
            habitDate.set(Calendar.HOUR_OF_DAY, 0)
            habitDate.set(Calendar.MINUTE, 0)
            habitDate.set(Calendar.SECOND, 0)
            habitDate.set(Calendar.MILLISECOND, 0)
            habitDate == today
        }
        
        todayHabits.postValue(todayHabitsList)
    }
    
    private fun updateHydrationLevel() {
        val hydration = dataManager.getHydration()
        val totalIntake = hydration * 0.25 // Convert glasses to liters
        hydrationLevel.postValue(totalIntake)
    }
    
    private fun updateHabitCompletion() {
        val habits = dataManager.getHabits()
        val completions = dataManager.getHabitCompletions()
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)
        
        val todayCompletions = completions.filter { completion ->
            val completionDate = Calendar.getInstance()
            completionDate.timeInMillis = completion.createdAt
            completionDate.set(Calendar.HOUR_OF_DAY, 0)
            completionDate.set(Calendar.MINUTE, 0)
            completionDate.set(Calendar.SECOND, 0)
            completionDate.set(Calendar.MILLISECOND, 0)
            completionDate == today
        }
        
        val completedCount = todayCompletions.count { it.isActive }
        val totalCount = habits.size
        val completionPercentage = if (totalCount > 0) (completedCount * 100) / totalCount else 0
        
        habitCompletion.postValue(completionPercentage)
    }
    
    private fun updateMoodTrend() {
        val moods = dataManager.getMoods()
        val last7Days = moods.sortedByDescending { it.date }.take(7)
        moodTrend.postValue(last7Days)
    }
    
    // Public methods for manual updates
    fun addMood(mood: Mood) {
        dataManager.addMood(mood)
        updateCurrentMood()
        updateMoodTrend()
    }
    
    fun addHydration(hydration: Hydration) {
        dataManager.saveHydration(hydration.glassesDrank)
        updateHydrationLevel()
    }
    
    fun completeHabit(habitId: String) {
        val today = Date()
        dataManager.markHabitComplete(habitId)
        updateHabitCompletion()
        updateTodayHabits()
    }
    
    fun getCurrentMood(): MutableLiveData<Mood> = currentMood
    fun getTodayHabits(): MutableLiveData<List<Habit>> = todayHabits
    fun getHydrationLevel(): MutableLiveData<Double> = hydrationLevel
    fun getHabitCompletion(): MutableLiveData<Int> = habitCompletion
    fun getMoodTrend(): MutableLiveData<List<Mood>> = moodTrend
    fun getServiceStatus(): MutableLiveData<Boolean> = isServiceRunning
}
