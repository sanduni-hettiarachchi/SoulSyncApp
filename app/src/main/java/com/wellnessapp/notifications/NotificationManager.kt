package com.wellnessapp.notifications

import android.content.Context
import androidx.work.*
import java.util.concurrent.TimeUnit

/**
 * Manager class for handling all app notifications
 * Manages WorkManager scheduling for hydration reminders and other notifications
 */
class NotificationManager(private val context: Context) {

    private val workManager = WorkManager.getInstance(context)

    companion object {
        const val HYDRATION_WORK_NAME = "hydration_reminder_work"
        const val DEFAULT_INTERVAL_HOURS = 2L // Default 2-hour intervals
    }

    /**
     * Schedules hydration reminders at specified intervals
     * @param intervalHours Interval between reminders in hours
     */
    fun scheduleHydrationReminders(intervalHours: Long = DEFAULT_INTERVAL_HOURS) {
        // Cancel existing work
        cancelHydrationReminders()

        // Create constraints for the work
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(true)
            .build()

        // Create periodic work request
        val hydrationWork = PeriodicWorkRequestBuilder<HydrationReminderWorker>(
            intervalHours, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()

        // Enqueue the work
        workManager.enqueueUniquePeriodicWork(
            HYDRATION_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            hydrationWork
        )
    }

    /**
     * Schedules hydration reminders using minute granularity (>=15 minutes due to WorkManager limits)
     * @param intervalMinutes Interval between reminders in minutes
     */
    fun scheduleHydrationRemindersMinutes(intervalMinutes: Long) {
        // WorkManager periodic jobs must be at least 15 minutes apart
        val minInterval = 15L
        val normalizedMinutes = intervalMinutes.coerceAtLeast(minInterval)
        
        // Cancel existing work first
        cancelHydrationReminders()
        
        // Create constraints for the work
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(true)
            .build()
        
        // Create periodic work request using minutes directly for better accuracy
        val hydrationWork = PeriodicWorkRequestBuilder<HydrationReminderWorker>(
            normalizedMinutes, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.LINEAR,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .build()
        
        // Enqueue the work
        workManager.enqueueUniquePeriodicWork(
            HYDRATION_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            hydrationWork
        )
        
        android.util.Log.d("NotificationManager", "Hydration reminders scheduled: every $normalizedMinutes minutes")
    }

    /**
     * Cancels all hydration reminder notifications
     */
    fun cancelHydrationReminders() {
        workManager.cancelUniqueWork(HYDRATION_WORK_NAME)
    }

    /**
     * Schedules a one-time hydration reminder
     * @param delayMinutes Delay in minutes before showing the reminder
     */
    fun scheduleOneTimeHydrationReminder(delayMinutes: Long) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .build()

        val oneTimeWork = OneTimeWorkRequestBuilder<HydrationReminderWorker>()
            .setConstraints(constraints)
            .setInitialDelay(delayMinutes, TimeUnit.MINUTES)
            .build()

        workManager.enqueue(oneTimeWork)
    }

    /**
     * Checks if hydration reminders are currently scheduled
     */
    fun isHydrationRemindersScheduled(): Boolean {
        val workInfos = workManager.getWorkInfosForUniqueWork(HYDRATION_WORK_NAME).get()
        return workInfos.isNotEmpty() && workInfos.any { it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING }
    }
}
