package com.wellnessapp.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.wellnessapp.MainActivity
import com.wellnessapp.R
import com.wellnessapp.data.DataManager

/**
 * Home screen widget provider for SoulSync
 * 
 * Features:
 * - Displays daily habit completion percentage
 * - Shows current hydration progress
 * - Quick access to main app
 * - Real-time updates
 */
class WellnessWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Update all widget instances
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Called when the first widget is created
        super.onEnabled(context)
    }

    override fun onDisabled(context: Context) {
        // Called when the last widget is removed
        super.onDisabled(context)
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val dataManager = DataManager.getInstance(context)
            
            // Get current data
            val completionPercentage = dataManager.getDailyCompletionPercentage()
            val hydrationCurrent = dataManager.getCurrentHydration()
            val hydrationGoal = dataManager.getHydrationGoal()
            val habits = dataManager.getHabits()
            val activeHabits = habits.count { it.isActive }

            // Create widget layout
            val views = RemoteViews(context.packageName, R.layout.wellness_widget)

            // Update progress text
            views.setTextViewText(R.id.tvWidgetProgress, "$completionPercentage%")
            views.setTextViewText(R.id.tvWidgetHydration, "$hydrationCurrent/$hydrationGoal")
            views.setTextViewText(R.id.tvWidgetHabits, "$activeHabits habits")

            // Set progress bar
            views.setProgressBar(R.id.progressBarWidget, 100, completionPercentage, false)

            // Set click intent to open main app
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widgetContainer, pendingIntent)

            // Update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
