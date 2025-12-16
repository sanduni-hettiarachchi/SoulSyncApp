package com.wellnessapp.ui.habits

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wellnessapp.data.DataManager
import com.wellnessapp.data.Habit
import java.util.*

/**
 * ViewModel for Habits Fragment
 * Manages habit data and operations
 */
class HabitsViewModel : ViewModel() {
    
    private val _habits = MutableLiveData<List<Habit>>()
    val habits: LiveData<List<Habit>> = _habits
    
    private val _habitsCompletionPercentage = MutableLiveData<Int>()
    val habitsCompletionPercentage: LiveData<Int> = _habitsCompletionPercentage
    
    private lateinit var dataManager: DataManager
    
    fun setDataManager(dataManager: DataManager) {
        this.dataManager = dataManager
    }
    
    fun loadHabits(dataManager: DataManager) {
        this.dataManager = dataManager
        val habitsList = dataManager.getHabits()
        _habits.value = habitsList
        updateCompletionPercentage()
    }
    
    fun toggleHabitCompletion(habit: Habit) {
        val today = Date()
        if (isHabitCompletedForToday(habit.id)) {
            dataManager.markHabitIncomplete(habit.id)
        } else {
            dataManager.markHabitComplete(habit.id)
        }
        updateCompletionPercentage()
    }
    
    fun deleteHabit(habitId: String) {
        dataManager.deleteHabit(habitId)
        loadHabits(dataManager)
    }
    
    fun isHabitCompletedForToday(habitId: String): Boolean {
        val today = Date()
        val completion = dataManager.getHabitCompletionForDate(today.time)
        return completion.any { it.id == habitId && it.isActive }
    }
    
    private fun updateCompletionPercentage() {
        val habitsList = _habits.value ?: emptyList()
        val today = Date()
        val completedCount = habitsList.count { habit ->
            isHabitCompletedForToday(habit.id)
        }
        val percentage = if (habitsList.isNotEmpty()) {
            (completedCount * 100) / habitsList.size
        } else {
            0
        }
        _habitsCompletionPercentage.value = percentage
    }
}