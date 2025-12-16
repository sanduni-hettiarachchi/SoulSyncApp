package com.wellnessapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.wellnessapp.data.DataManager
import com.wellnessapp.databinding.ActivitySettingsBinding
import com.wellnessapp.notifications.NotificationManager

/**
 * Settings Activity for configuring app preferences
 * 
 * Features:
 * - User profile management
 * - Hydration reminder settings
 * - App appearance settings
 * - Data management (export, share, reset)
 * - Real-time settings persistence
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var dataManager: DataManager
    private lateinit var notificationManager: NotificationManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Hide the action bar to remove "SoulSync" text
        supportActionBar?.hide()
        
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize managers
        dataManager = DataManager.getInstance(this)
        notificationManager = NotificationManager(this)

        setupUI()
        loadCurrentSettings()
        setupListeners()
    }

    /**
     * Sets up the UI components
     */
    private fun setupUI() {
        // Set up back button
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    /**
     * Loads current settings from DataManager
     */
    private fun loadCurrentSettings() {
        // Load user name
        binding.etUserName.setText(dataManager.getUserName())

        // Load hydration settings
        binding.etHydrationGoal.setText(dataManager.getHydrationGoal().toString())
        
        val (notificationsEnabled, intervalHours) = dataManager.getNotificationSettings()
        binding.switchHydrationReminders.isChecked = notificationsEnabled
        binding.etReminderInterval.setText(intervalHours.toString())

        // Load app settings (these would be stored in SharedPreferences)
        // For now, we'll use default values
        binding.switchDarkMode.isChecked = false
        binding.switchHapticFeedback.isChecked = true
        binding.switchAutoSync.isChecked = true
    }

    /**
     * Sets up click listeners for all interactive elements
     */
    private fun setupListeners() {
        // Save settings button
        binding.btnSaveSettings.setOnClickListener {
            saveSettings()
        }

        // Data management buttons
        binding.btnExportData.setOnClickListener {
            exportData()
        }

        binding.btnShareProgress.setOnClickListener {
            shareProgress()
        }

        binding.btnResetData.setOnClickListener {
            showResetDataDialog()
        }

        // Hydration reminder toggle
        binding.switchHydrationReminders.setOnCheckedChangeListener { _, isChecked ->
            binding.etReminderInterval.isEnabled = isChecked
            // Auto-save when toggle changes
            saveSettings()
        }
        
        // Auto-save when text fields change
        binding.etUserName.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                saveSettings()
            }
        }
        
        binding.etHydrationGoal.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                saveSettings()
            }
        }
        
        binding.etReminderInterval.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                saveSettings()
            }
        }
        
        // Auto-save when app settings change
        binding.switchDarkMode.setOnCheckedChangeListener { _, _ ->
            saveSettings()
        }
        
        binding.switchHapticFeedback.setOnCheckedChangeListener { _, _ ->
            saveSettings()
        }
        
        binding.switchAutoSync.setOnCheckedChangeListener { _, _ ->
            saveSettings()
        }
    }

    /**
     * Saves all settings to DataManager
     */
    private fun saveSettings() {
        try {
            // Save user name
            val userName = binding.etUserName.text.toString().trim()
            if (userName.isNotEmpty()) {
                dataManager.setUserName(userName)
                android.util.Log.d("SettingsActivity", "User name updated to: $userName")
            }

            // Save hydration settings
            val hydrationGoal = binding.etHydrationGoal.text.toString().toIntOrNull() ?: 8
            dataManager.setHydrationGoal(hydrationGoal)
            android.util.Log.d("SettingsActivity", "Hydration goal updated to: $hydrationGoal")

            val notificationsEnabled = binding.switchHydrationReminders.isChecked
            val intervalHours = binding.etReminderInterval.text.toString().toIntOrNull() ?: 2
            dataManager.setNotificationSettings(notificationsEnabled, intervalHours)

            // Update notification schedule
            if (notificationsEnabled) {
                notificationManager.scheduleHydrationReminders(intervalHours.toLong())
                android.util.Log.d("SettingsActivity", "Hydration reminders enabled: every $intervalHours hours")
            } else {
                notificationManager.cancelHydrationReminders()
                android.util.Log.d("SettingsActivity", "Hydration reminders disabled")
            }

            // Save app settings (would be stored in SharedPreferences)
            // This is a simplified version - in a real app, you'd have a proper settings manager

            // Show subtle confirmation (only if not auto-saving)
            if (binding.btnSaveSettings.hasFocus()) {
                Toast.makeText(this, "Settings saved successfully!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                // Silent auto-save - just log
                android.util.Log.d("SettingsActivity", "Settings auto-saved")
            }

        } catch (e: Exception) {
            android.util.Log.e("SettingsActivity", "Error saving settings: ${e.message}", e)
            Toast.makeText(this, "Error saving settings: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Exports user data (simplified implementation)
     */
    private fun exportData() {
        try {
            val habits = dataManager.getHabits()
            val moods = dataManager.getMoods()
            val hydrationGoal = dataManager.getHydrationGoal()

            val exportData = buildString {
                appendLine("SoulSync Data Export")
                appendLine("===================")
                appendLine()
                appendLine("Habits (${habits.size}):")
                habits.forEach { habit ->
                    appendLine("- ${habit.name}: ${habit.target}")
                }
                appendLine()
                appendLine("Moods (${moods.size}):")
                moods.takeLast(10).forEach { mood ->
                    val date = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
                        .format(java.util.Date(mood.date))
                    appendLine("- $date: ${mood.emoji} ${mood.id}")
                }
                appendLine()
                appendLine("Hydration Goal: $hydrationGoal glasses per day")
            }

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, exportData)
                putExtra(Intent.EXTRA_SUBJECT, "My SoulSync Progress")
            }

            startActivity(Intent.createChooser(shareIntent, "Export Data"))

        } catch (e: Exception) {
            Toast.makeText(this, "Error exporting data: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Shares progress summary
     */
    private fun shareProgress() {
        try {
            val habits = dataManager.getHabits()
            val completionPercentage = dataManager.getDailyCompletionPercentage()
            val userName = dataManager.getUserName().ifEmpty { "User" }

            val progressText = buildString {
                appendLine("🌟 My SoulSync Progress")
                appendLine("=======================")
                appendLine()
                appendLine("Hey! I'm using SoulSync to track my wellness journey.")
                appendLine()
                appendLine("Today's Completion: $completionPercentage%")
                appendLine("Active Habits: ${habits.count { it.isActive }}")
                appendLine()
                appendLine("Download SoulSync to start your wellness journey! 💚")
            }

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, progressText)
                putExtra(Intent.EXTRA_SUBJECT, "My SoulSync Progress")
            }

            startActivity(Intent.createChooser(shareIntent, "Share Progress"))

        } catch (e: Exception) {
            Toast.makeText(this, "Error sharing progress: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Shows confirmation dialog for resetting all data
     */
    private fun showResetDataDialog() {
        AlertDialog.Builder(this)
            .setTitle("Reset All Data")
            .setMessage("Are you sure you want to reset all your data? This action cannot be undone.")
            .setPositiveButton("Reset") { _, _ ->
                resetAllData()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    /**
     * Resets all user data
     */
    private fun resetAllData() {
        try {
            dataManager.clearAllData()
            notificationManager.cancelHydrationReminders()
            
            Toast.makeText(this, "All data has been reset", Toast.LENGTH_SHORT).show()
            finish()

        } catch (e: Exception) {
            Toast.makeText(this, "Error resetting data: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        // Check if there are unsaved changes
        if (hasUnsavedChanges()) {
            AlertDialog.Builder(this)
                .setTitle("Unsaved Changes")
                .setMessage("You have unsaved changes. Do you want to save them?")
                .setPositiveButton("Save") { _, _ ->
                    saveSettings()
                }
                .setNegativeButton("Discard") { _, _ ->
                    super.onBackPressed()
                }
                .setNeutralButton("Cancel", null)
                .show()
        } else {
            super.onBackPressed()
        }
    }

    /**
     * Checks if there are unsaved changes
     */
    private fun hasUnsavedChanges(): Boolean {
        val currentUserName = dataManager.getUserName()
        val currentHydrationGoal = dataManager.getHydrationGoal()
        val (currentNotificationsEnabled, currentInterval) = dataManager.getNotificationSettings()

        val newUserName = binding.etUserName.text.toString().trim()
        val newHydrationGoal = binding.etHydrationGoal.text.toString().toIntOrNull() ?: 8
        val newNotificationsEnabled = binding.switchHydrationReminders.isChecked
        val newInterval = binding.etReminderInterval.text.toString().toIntOrNull() ?: 2

        return newUserName != currentUserName ||
                newHydrationGoal != currentHydrationGoal ||
                newNotificationsEnabled != currentNotificationsEnabled ||
                newInterval != currentInterval
    }
}
