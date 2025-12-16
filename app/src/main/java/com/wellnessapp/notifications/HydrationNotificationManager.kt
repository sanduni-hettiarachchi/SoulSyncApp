package com.wellnessapp.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.*
import com.wellnessapp.MainActivity
import com.wellnessapp.R
import com.wellnessapp.data.DataManager
import java.util.concurrent.TimeUnit

/**
 * Manages hydration reminder notifications
 */
class HydrationNotificationManager(private val context: Context) {
    
    companion object {
        private const val CHANNEL_ID = "hydration_reminders"
        private const val CHANNEL_NAME = "Hydration Reminders"
        private const val CHANNEL_DESCRIPTION = "Reminders to drink water throughout the day"
        private const val NOTIFICATION_ID = 1001
    }
    
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val workManager = WorkManager.getInstance(context)
    private val dataManager = DataManager.getInstance(context)
    
    init {
        createNotificationChannel()
    }
    
    /**
     * Create notification channel for hydration reminders
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
                enableVibration(true)
                enableLights(true)
            }
            
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Show hydration reminder notification
     */
    fun showHydrationReminder(currentGlasses: Int, goalGlasses: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val remaining = goalGlasses - currentGlasses
        val message = if (remaining > 0) {
            "You've had $currentGlasses glasses today. $remaining more to reach your goal of $goalGlasses!"
        } else {
            "Great job! You've reached your daily hydration goal of $goalGlasses glasses!"
        }
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_water)
            .setContentTitle("💧 Time to Hydrate!")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
    
    /**
     * Show goal achievement notification
     */
    fun showGoalAchievementNotification(goalGlasses: Int) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_water)
            .setContentTitle("🎉 Hydration Goal Achieved!")
            .setContentText("Congratulations! You've reached your daily goal of $goalGlasses glasses!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setCategory(NotificationCompat.CATEGORY_STATUS)
            .build()
        
        notificationManager.notify(NOTIFICATION_ID + 1, notification)
    }
    
    /**
     * Schedule hydration reminders using WorkManager
     * Uses saved interval from SharedPreferences (in minutes)
     */
    fun scheduleHydrationReminders() {
        if (!dataManager.getReminderEnabled()) {
            cancelHydrationReminders()
            return
        }
        
        // Cancel existing reminders first
        cancelHydrationReminders()
        
        // Get saved interval from SharedPreferences (default to 120 minutes / 2 hours)
        val sharedPrefs = context.getSharedPreferences("wellness_app", Context.MODE_PRIVATE)
        val intervalMinutes = sharedPrefs.getLong("hydration_reminder_interval_minutes", 120L)
        
        // WorkManager requires minimum 15 minutes for periodic work
        val safeIntervalMinutes = intervalMinutes.coerceAtLeast(15L)
        
        // Create periodic work request for hydration reminders
        val hydrationReminderRequest = PeriodicWorkRequestBuilder<HydrationReminderWorker>(
            safeIntervalMinutes, TimeUnit.MINUTES
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .addTag("hydration_reminder")
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            "hydration_reminder_work",
            ExistingPeriodicWorkPolicy.REPLACE,
            hydrationReminderRequest
        )
        
        android.util.Log.d("HydrationNotificationManager", "Hydration reminders scheduled: every $safeIntervalMinutes minutes")
    }
    
    /**
     * Cancel hydration reminders
     */
    fun cancelHydrationReminders() {
        workManager.cancelUniqueWork("hydration_reminder_work")
        workManager.cancelAllWorkByTag("hydration_reminder")
        android.util.Log.d("HydrationNotificationManager", "Hydration reminders cancelled")
    }
    
    /**
     * Cancel all hydration notifications
     */
    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }
}
