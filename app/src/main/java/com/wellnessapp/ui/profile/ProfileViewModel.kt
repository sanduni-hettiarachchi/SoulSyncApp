package com.wellnessapp.ui.profile

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wellnessapp.data.DataManager

/**
 * ViewModel for Profile Fragment
 * Manages user profile data and settings
 */
class ProfileViewModel : ViewModel() {
    
    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> = _userName
    
    private val _totalHabits = MutableLiveData<Int>()
    val totalHabits: LiveData<Int> = _totalHabits
    
    private val _completedHabits = MutableLiveData<Int>()
    val completedHabits: LiveData<Int> = _completedHabits
    
    private lateinit var dataManager: DataManager
    
    fun setDataManager(dataManager: DataManager) {
        this.dataManager = dataManager
        loadUserData()
    }
    
    private fun loadUserData() {
        _userName.value = dataManager.getUserName()
        _totalHabits.value = dataManager.getHabits().size
        // Calculate completed habits for today
        val today = java.util.Date()
        val completedCount = dataManager.getHabitsCompletedForDate(today.time).size
        _completedHabits.value = completedCount
    }
    
    fun updateUserName(name: String) {
        dataManager.setUserName(name)
        _userName.value = name
    }
    
    fun refreshData() {
        loadUserData()
    }
}