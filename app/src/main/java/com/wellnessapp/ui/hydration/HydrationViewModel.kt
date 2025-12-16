package com.wellnessapp.ui.hydration

import androidx.lifecycle.ViewModel
import com.wellnessapp.data.DataManager

/**
 * ViewModel for Hydration Fragment
 */
class HydrationViewModel : ViewModel() {
    
    private lateinit var dataManager: DataManager
    
    fun initializeDataManager(context: android.content.Context) {
        dataManager = DataManager.getInstance(context)
    }
    
    fun getHydration(): Int {
        return if (::dataManager.isInitialized) {
            dataManager.getHydration()
        } else {
            0
        }
    }
    
    fun saveHydration(glasses: Int) {
        if (::dataManager.isInitialized) {
            dataManager.saveHydration(glasses)
        }
    }
}
