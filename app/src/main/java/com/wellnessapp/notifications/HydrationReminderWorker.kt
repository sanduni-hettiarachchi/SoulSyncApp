package com.wellnessapp.notifications

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import android.util.Log

/**
 * WorkManager worker for hydration reminders
 * Handles scheduled background tasks for water drinking notifications
 */
class HydrationReminderWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {

    override fun doWork(): Result {
        Log.d("HydrationWorker", "Hydration reminder worker started.")
        
        try {
            val notificationManager = HydrationNotificationManager(applicationContext)
            
            // Get current hydration data
            val dataManager = com.wellnessapp.data.DataManager.getInstance(applicationContext)
            val currentGlasses = dataManager.getHydration()
            val goalGlasses = dataManager.getHydrationGoal()
            
            // Show appropriate notification based on progress
            if (currentGlasses < goalGlasses) {
                notificationManager.showHydrationReminder(currentGlasses, goalGlasses)
                Log.d("HydrationWorker", "Showed hydration reminder: $currentGlasses/$goalGlasses glasses")
            } else {
                notificationManager.showGoalAchievementNotification(goalGlasses)
                Log.d("HydrationWorker", "Showed goal achievement notification for $goalGlasses glasses")
            }
            
            Log.d("HydrationWorker", "Hydration reminder notification shown successfully.")
            return Result.success()
        } catch (e: Exception) {
            Log.e("HydrationWorker", "Error showing hydration reminder: ${e.message}", e)
            return Result.retry()
        }
    }
}