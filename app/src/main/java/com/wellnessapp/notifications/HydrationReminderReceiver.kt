package com.wellnessapp.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * BroadcastReceiver for hydration reminder notifications
 * Handles scheduled hydration reminders
 */
class HydrationReminderReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("HydrationReminderReceiver", "Received hydration reminder broadcast")
        
        try {
            val notificationManager = HydrationNotificationManager(context)
            val dataManager = com.wellnessapp.data.DataManager.getInstance(context)
            
            val currentGlasses = dataManager.getHydration()
            val goalGlasses = dataManager.getHydrationGoal()
            
            // Show notification based on current progress
            if (currentGlasses < goalGlasses) {
                notificationManager.showHydrationReminder(currentGlasses, goalGlasses)
            } else {
                notificationManager.showGoalAchievementNotification(goalGlasses)
            }
            
            Log.d("HydrationReminderReceiver", "Hydration reminder notification displayed")
        } catch (e: Exception) {
            Log.e("HydrationReminderReceiver", "Error handling hydration reminder: ${e.message}", e)
        }
    }
}
