package com.wellnessapp.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wellnessapp.data.DataManager
import com.wellnessapp.data.Habit
import com.wellnessapp.data.Mood
import java.util.*

/**
 * ViewModel for Home Fragment
 * Manages home screen data and user interactions
 */
class HomeViewModel : ViewModel() {
    
    private val _habitsCompleted = MutableLiveData<Int>()
    val habitsCompleted: LiveData<Int> = _habitsCompleted
    
    private val _currentMood = MutableLiveData<Mood?>()
    val currentMood: LiveData<Mood?> = _currentMood
    
    private val _todayHabits = MutableLiveData<List<Habit>>()
    val todayHabits: LiveData<List<Habit>> = _todayHabits
    
    fun logMood(mood: Mood) {
        _currentMood.value = mood
    }
    
    fun updateHabitsCompleted(count: Int) {
        _habitsCompleted.value = count
    }
    
    fun updateTodayHabits(habits: List<Habit>) {
        _todayHabits.value = habits
    }
}