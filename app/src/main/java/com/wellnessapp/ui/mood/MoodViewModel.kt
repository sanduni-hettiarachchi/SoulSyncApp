package com.wellnessapp.ui.mood

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wellnessapp.data.DataManager
import com.wellnessapp.data.Mood

class MoodViewModel : ViewModel() {
    
    private var dataManager: DataManager? = null
    
    private val _moodHistory = MutableLiveData<List<Mood>>()
    val moodHistory: LiveData<List<Mood>> = _moodHistory
    
    fun setDataManager(dataManager: DataManager) {
        this.dataManager = dataManager
        loadMoodHistory()
    }
    
    private fun loadMoodHistory() {
        dataManager?.let { dm ->
            val moods = dm.getMoodsForToday()
            _moodHistory.value = moods
        }
    }
    
    fun refreshMoodHistory() {
        loadMoodHistory()
    }
}