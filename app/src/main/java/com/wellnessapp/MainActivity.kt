package com.wellnessapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import java.util.UUID
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.wellnessapp.databinding.ActivityMainBinding
import com.wellnessapp.ui.mood.MoodDetailActivity
import com.wellnessapp.notifications.NotificationManager

/**
 * Main Activity for the Wellness Tracker app
 * Handles navigation between different fragments and provides quick access to common actions
 */
class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    // FAB removed from layout
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            android.util.Log.d("MainActivity", "onCreate started")
            
            // Hide the action bar to remove "SoulSync" text
            supportActionBar?.hide()
            android.util.Log.d("MainActivity", "Action bar hidden")
            
        // Configure status bar for light content (black text/icons on light backgrounds)
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        // Use modern API for better performance
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                android.view.WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
            android.util.Log.d("MainActivity", "Status bar configured for light content")
        
        // Check if user has completed onboarding
        val sharedPrefs = getSharedPreferences("wellness_app", Context.MODE_PRIVATE)
        
        val hasCompletedOnboarding = sharedPrefs.getBoolean("onboarding_completed", false)
        val isUserSignedIn = sharedPrefs.getBoolean("user_signed_in", false)
            
            android.util.Log.d("MainActivity", "Onboarding completed: $hasCompletedOnboarding")
            android.util.Log.d("MainActivity", "User signed in: $isUserSignedIn")
        
        // Check onboarding first - this is the correct flow: Onboarding → Sign-in → Home
        if (!hasCompletedOnboarding) {
            // Start onboarding flow first
            android.util.Log.d("MainActivity", "Starting onboarding flow - hasCompletedOnboarding: $hasCompletedOnboarding")
            try {
                val intent = Intent(this, OnboardingDrinkActivity::class.java)
                android.util.Log.d("MainActivity", "Intent created: $intent")
                startActivity(intent)
                android.util.Log.d("MainActivity", "Activity started successfully")
                finish()
                return
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error starting onboarding: ${e.message}", e)
                // If onboarding fails, try sign-in as fallback
                android.util.Log.d("MainActivity", "Falling back to sign-in")
                startActivity(Intent(this, SignInActivity::class.java))
                finish()
                return
            }
        }
        
        // If onboarding is completed, check if user is signed in
        if (!isUserSignedIn) {
            // User needs to sign in
            android.util.Log.d("MainActivity", "Onboarding completed but user not signed in, redirecting to sign in")
            startActivity(Intent(this, SignInActivity::class.java))
            finish()
            return
        }
        
        // Both onboarding and sign-in are completed, show main app
        android.util.Log.d("MainActivity", "User signed in and onboarding completed, showing main app")
            
            try {
        binding = ActivityMainBinding.inflate(layoutInflater)
                android.util.Log.d("MainActivity", "Binding created successfully")
        setContentView(binding.root)
                android.util.Log.d("MainActivity", "Content view set successfully")
        
        setupNavigation()
                android.util.Log.d("MainActivity", "Navigation setup complete")
                
                // Setup notifications in background to prevent ANR (with delay to avoid blocking UI)
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    try {
                        setupNotifications()
                        android.util.Log.d("MainActivity", "Notifications setup complete")
                    } catch (e: Exception) {
                        android.util.Log.e("MainActivity", "Error setting up notifications: ${e.message}", e)
                    }
                }, 500) // Small delay to ensure UI is fully rendered
                android.util.Log.d("MainActivity", "onCreate completed successfully")
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Error setting up main app: ${e.message}", e)
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error in onCreate: ${e.message}", e)
        }
    }
    
    
    /**
     * Set up navigation between fragments
     */
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        
        // Set up bottom navigation
        binding.bottomNavigation.setupWithNavController(navController)
        
        // Let NavigationUI handle navigation automatically
        android.util.Log.d("MainActivity", "Bottom navigation setup completed")
        
        // Navigation setup complete - no action bar needed
    }
    
    // FAB setup removed - no FAB in current layout
    
    /**
     * Show quick add menu with options for adding habits, moods, etc.
     */
    private fun showQuickAddMenu() {
        val currentDestination = navController.currentDestination?.id
        
        when (currentDestination) {
            R.id.nav_home -> {
                // On home screen, show options for both habits and moods
                showQuickAddDialog()
            }
            R.id.nav_stats -> {
                // On stats screen, navigate to home for quick add
                navController.navigate(R.id.nav_home)
            }
            R.id.nav_calendar -> {
                // On calendar screen, navigate to home for quick add
                navController.navigate(R.id.nav_home)
            }
            R.id.nav_profile -> {
                // On profile screen, navigate to home for quick add
                navController.navigate(R.id.nav_home)
            }
        }
    }
    
    /**
     * Show dialog with quick add options
     */
    private fun showQuickAddDialog() {
        val options = arrayOf(
            "Add Habit",
            "Add Mood",
            "Add Water",
            "Hydration Settings"
        )
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Quick Add")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showAddHabitDialog()
                    1 -> startActivity(Intent(this, MoodDetailActivity::class.java))
                    2 -> addGlassOfWater()
                    3 -> showHydrationSettings()
                }
            }
            .show()
    }
    
    /**
     * Show add habit dialog
     */
    private fun showAddHabitDialog() {
        try {
            val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_habit, null)
            
            // Use TextInputEditText as per the layout file
            val etHabitName = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_habit_name)
            val etHabitDescription = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_habit_description)
            val spHabitType = dialogView.findViewById<Spinner>(R.id.spinner_habit_type)
            val etHabitDuration = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_habit_duration)
            
            // Set up habit type spinner
            val habitTypes = arrayOf("Exercise", "Wellness", "Learning", "Productivity", "Health", "Social")
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, habitTypes)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spHabitType.adapter = adapter
            
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setView(dialogView)
                .setTitle("Add New Habit")
                .setPositiveButton("Save") { _, _ ->
                    // Run in background to prevent UI lag
                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                        try {
                            val habitName = etHabitName?.text?.toString()?.trim() ?: ""
                            val habitDescription = etHabitDescription?.text?.toString()?.trim() ?: ""
                            val habitType = spHabitType?.selectedItem?.toString() ?: "Wellness"
                            val durationText = etHabitDuration?.text?.toString()?.trim() ?: "30"
                            val duration = durationText.toIntOrNull() ?: 30
                            
                            if (habitName.isNotEmpty()) {
                                // Initialize DataManager
                                val dataManager = com.wellnessapp.data.DataManager.getInstance(this)
                                
                                // Create new habit
                                val habit = com.wellnessapp.data.Habit(
                                    id = "habit_${System.currentTimeMillis()}",
                                    name = habitName,
                                    description = habitDescription,
                                    target = "$duration min",
                                    type = habitType,
                                    createdAt = System.currentTimeMillis(),
                                    isActive = true
                                )
                                
                                // Save habit to DataManager
                                dataManager.saveHabit(habit)
                                
                                Toast.makeText(this, "Habit '$habitName' added successfully!", Toast.LENGTH_SHORT).show()
                                android.util.Log.d("MainActivity", "Habit saved: $habitName")
                            } else {
                                Toast.makeText(this, "Please enter a habit name", Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: Exception) {
                            android.util.Log.e("MainActivity", "Error saving habit: ${e.message}", e)
                            Toast.makeText(this, "Error saving habit: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error showing add habit dialog: ${e.message}", e)
            Toast.makeText(this, "Error opening dialog: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Add a glass of water to hydration tracking
     */
    private fun addGlassOfWater() {
        val dataManager = com.wellnessapp.data.DataManager.getInstance(this)
        val currentHydration = dataManager.getHydration()
        val newHydration = currentHydration + 1
        dataManager.saveHydration(newHydration)
        
        Toast.makeText(this, "Glass of water added! 💧 ($newHydration/8 glasses)", Toast.LENGTH_SHORT).show()
    }
    
    /**
     * Show hydration settings dialog with minute-based options
     */
    private fun showHydrationSettings() {
        val options = arrayOf(
            "Every 15 minutes",
            "Every 30 minutes",
            "Every 60 minutes (1 hour)",
            "Every 120 minutes (2 hours)",
            "Every 180 minutes (3 hours)",
            "Disable Reminders"
        )
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Hydration Reminder Settings")
            .setItems(options) { _, which ->
                // Run in background to prevent UI lag
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    try {
                        val sharedPrefs = getSharedPreferences("wellness_app", Context.MODE_PRIVATE)
                        val notificationManager = NotificationManager(this)
                        
                        when (which) {
                            0 -> {
                                // 15 minutes
                                val minutes = 15L
                                sharedPrefs.edit()
                                    .putBoolean("hydration_reminders_enabled", true)
                                    .putLong("hydration_reminder_interval_minutes", minutes)
                                    .apply()
                                notificationManager.scheduleHydrationRemindersMinutes(minutes)
                                Toast.makeText(this, "Reminders enabled (every 15 minutes)", Toast.LENGTH_SHORT).show()
                            }
                            1 -> {
                                // 30 minutes
                                val minutes = 30L
                                sharedPrefs.edit()
                                    .putBoolean("hydration_reminders_enabled", true)
                                    .putLong("hydration_reminder_interval_minutes", minutes)
                                    .apply()
                                notificationManager.scheduleHydrationRemindersMinutes(minutes)
                                Toast.makeText(this, "Reminders enabled (every 30 minutes)", Toast.LENGTH_SHORT).show()
                            }
                            2 -> {
                                // 60 minutes (1 hour)
                                val minutes = 60L
                                sharedPrefs.edit()
                                    .putBoolean("hydration_reminders_enabled", true)
                                    .putLong("hydration_reminder_interval_minutes", minutes)
                                    .apply()
                                notificationManager.scheduleHydrationRemindersMinutes(minutes)
                                Toast.makeText(this, "Reminders enabled (every 1 hour)", Toast.LENGTH_SHORT).show()
                            }
                            3 -> {
                                // 120 minutes (2 hours)
                                val minutes = 120L
                                sharedPrefs.edit()
                                    .putBoolean("hydration_reminders_enabled", true)
                                    .putLong("hydration_reminder_interval_minutes", minutes)
                                    .apply()
                                notificationManager.scheduleHydrationRemindersMinutes(minutes)
                                Toast.makeText(this, "Reminders enabled (every 2 hours)", Toast.LENGTH_SHORT).show()
                            }
                            4 -> {
                                // 180 minutes (3 hours)
                                val minutes = 180L
                                sharedPrefs.edit()
                                    .putBoolean("hydration_reminders_enabled", true)
                                    .putLong("hydration_reminder_interval_minutes", minutes)
                                    .apply()
                                notificationManager.scheduleHydrationRemindersMinutes(minutes)
                                Toast.makeText(this, "Reminders enabled (every 3 hours)", Toast.LENGTH_SHORT).show()
                            }
                            5 -> {
                                // Disable
                                sharedPrefs.edit()
                                    .putBoolean("hydration_reminders_enabled", false)
                                    .apply()
                                notificationManager.cancelHydrationReminders()
                                Toast.makeText(this, "Hydration reminders disabled", Toast.LENGTH_SHORT).show()
                            }
                        }
                        android.util.Log.d("MainActivity", "Hydration reminder settings saved successfully")
                    } catch (e: Exception) {
                        android.util.Log.e("MainActivity", "Error saving hydration settings: ${e.message}", e)
                        Toast.makeText(this, "Error saving settings: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setOnDismissListener {
                // Ensure settings are persisted even if dialog is dismissed
                android.util.Log.d("MainActivity", "Hydration settings dialog dismissed")
            }
            .show()
    }
    
    /**
     * Set up notifications and reminders
     * This method is already called from a background handler, so no need to wrap again
     */
    private fun setupNotifications() {
        try {
            // Check if user wants hydration reminders
            val sharedPrefs = getSharedPreferences("wellness_app", Context.MODE_PRIVATE)
            val hydrationRemindersEnabled = sharedPrefs.getBoolean("hydration_reminders_enabled", true)
            val intervalMinutes = sharedPrefs.getLong("hydration_reminder_interval_minutes", 120L)

            if (hydrationRemindersEnabled && intervalMinutes >= 15) {
                val notificationManager = NotificationManager(this)
                // WorkManager periodic work requires minimum 15 minutes; clamp to avoid invalid values.
                val safeIntervalMinutes = intervalMinutes.coerceAtLeast(15L)
                notificationManager.scheduleHydrationRemindersMinutes(safeIntervalMinutes)
                android.util.Log.d("MainActivity", "Hydration reminders scheduled: every $safeIntervalMinutes minutes")
            } else {
                android.util.Log.d("MainActivity", "Hydration reminders disabled or invalid interval: $intervalMinutes")
            }
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error setting up notifications: ${e.message}", e)
        }
    }
    
    // Removed menu options - settings moved to profile page
    
    /**
     * Share wellness data using implicit intent
     */
    private fun shareWellnessData() {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Check out my wellness progress! 🌟")
            putExtra(Intent.EXTRA_SUBJECT, "My Wellness Journey")
        }
        startActivity(Intent.createChooser(shareIntent, "Share your wellness data"))
    }
    
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
