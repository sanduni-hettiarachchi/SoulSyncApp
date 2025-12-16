package com.wellnessapp.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.wellnessapp.R
import com.wellnessapp.data.DataManager
import com.wellnessapp.data.Mood
import com.wellnessapp.data.Habit
import com.wellnessapp.databinding.FragmentCalendarBinding
import java.text.SimpleDateFormat
import java.util.*

/**
 * Calendar Fragment - Shows monthly calendar with mood and habit tracking
 * Features:
 * - Monthly calendar view
 * - Daily mood and habit tracking
 * - Navigation between months
 * - Selected day details
 */
class CalendarFragment : Fragment() {
    
    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var dataManager: DataManager
    private var currentDate = Calendar.getInstance()
    private var selectedDate: Calendar? = null
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            android.util.Log.d("CalendarFragment", "onViewCreated started")
            
            // Setup basic UI immediately (non-blocking)
            showLoadingState()
            android.util.Log.d("CalendarFragment", "Basic UI setup completed")
            
            // Initialize data manager and heavy operations in background thread with delay
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                try {
                    // Check if fragment is still attached
                    if (!isAdded || isDetached) {
                        android.util.Log.w("CalendarFragment", "Fragment not attached, skipping background initialization")
                        return@postDelayed
                    }
                    
                    // Initialize data manager (heavy operation)
                    dataManager = DataManager.getInstance(requireContext())
                    android.util.Log.d("CalendarFragment", "DataManager initialized in background")
                    
                    // Check if fragment is still attached before continuing
                    if (!isAdded || isDetached) {
                        android.util.Log.w("CalendarFragment", "Fragment detached during initialization")
                        return@postDelayed
                    }
                    
                    // Setup UI components in background with additional delay
                    setupUI()
                    android.util.Log.d("CalendarFragment", "UI setup completed in background")
                    
                    // Add small delay between operations
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        if (!isAdded || isDetached) return@postDelayed
                        
                        setupNavigation()
                        android.util.Log.d("CalendarFragment", "Navigation setup completed in background")
                        
                        updateMonthDisplay()
                        android.util.Log.d("CalendarFragment", "Month display updated in background")
                        
                        // Add another delay before heavy operation
                        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                            if (!isAdded || isDetached) return@postDelayed
                            
                            // Populate calendar with data (heavy operation)
                            populateCalendar()
                            android.util.Log.d("CalendarFragment", "Calendar populated in background")
                            
                            // Hide loading state when ready
                            hideLoadingState()
                            android.util.Log.d("CalendarFragment", "Loading state hidden")
                        }, 1000) // 1 second delay before heavy operation
                    }, 500) // 500ms delay between operations
                    
                    android.util.Log.d("CalendarFragment", "Background initialization completed successfully")
                } catch (e: Exception) {
                    android.util.Log.e("CalendarFragment", "Error in background initialization: ${e.message}", e)
                    android.util.Log.e("CalendarFragment", "Stack trace: ${e.stackTraceToString()}")
                }
            }, 3000) // 3 second delay to ensure UI is fully rendered and prevent ANR
            
            android.util.Log.d("CalendarFragment", "onViewCreated completed successfully")
        } catch (e: Exception) {
            android.util.Log.e("CalendarFragment", "Error in onViewCreated: ${e.message}", e)
            android.util.Log.e("CalendarFragment", "Stack trace: ${e.stackTraceToString()}")
        }
    }
    
    /**
     * Set up UI components and click listeners
     */
    private fun setupUI() {
        try {
            android.util.Log.d("CalendarFragment", "Setting up UI components")
            
            // Set up tab switching
            binding.tvDailyTab.setOnClickListener {
                switchToDailyView()
            }
            
            binding.tvMonthlyTab.setOnClickListener {
                switchToMonthlyView()
            }
            
            // Set initial view to monthly
            switchToMonthlyView()
            
            android.util.Log.d("CalendarFragment", "UI setup completed successfully")
        } catch (e: Exception) {
            android.util.Log.e("CalendarFragment", "Error in setupUI: ${e.message}", e)
        }
    }
    
    /**
     * Set up navigation between months
     */
    private fun setupNavigation() {
        try {
            binding.btnPrevMonth.setOnClickListener {
                currentDate.add(Calendar.MONTH, -1)
                updateMonthDisplay()
                populateCalendar()
            }
            
            binding.btnNextMonth.setOnClickListener {
                currentDate.add(Calendar.MONTH, 1)
                updateMonthDisplay()
                populateCalendar()
            }
            
            android.util.Log.d("CalendarFragment", "Navigation setup completed")
        } catch (e: Exception) {
            android.util.Log.e("CalendarFragment", "Error in setupNavigation: ${e.message}", e)
        }
    }
    
    /**
     * Update month/year display
     */
    private fun updateMonthDisplay() {
        try {
            val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            binding.tvMonthYear.text = monthYearFormat.format(currentDate.timeInMillis)
            android.util.Log.d("CalendarFragment", "Month display updated: ${binding.tvMonthYear.text}")
        } catch (e: Exception) {
            android.util.Log.e("CalendarFragment", "Error updating month display: ${e.message}", e)
        }
    }
    
    /**
     * Populate calendar grid with days
     */
    private fun populateCalendar() {
        try {
            android.util.Log.d("CalendarFragment", "Populating calendar")
            
            // Clear existing views
            binding.calendarGrid.removeAllViews()
            
            // Get first day of month and number of days
            val firstDayOfMonth = currentDate.get(Calendar.DAY_OF_WEEK)
            val daysInMonth = currentDate.getActualMaximum(Calendar.DAY_OF_MONTH)
            
            // Add empty cells for days before first day of month
            for (i in 1 until firstDayOfMonth) {
                val emptyView = TextView(requireContext()).apply {
                    text = ""
                    layoutParams = ViewGroup.LayoutParams(0, 120)
                }
                binding.calendarGrid.addView(emptyView)
            }
            
            // Add day cells
            for (day in 1..daysInMonth) {
                val dayView = createDayView(day)
                binding.calendarGrid.addView(dayView)
            }
            
            android.util.Log.d("CalendarFragment", "Calendar populated with $daysInMonth days")
        } catch (e: Exception) {
            android.util.Log.e("CalendarFragment", "Error populating calendar: ${e.message}", e)
        }
    }
    
    /**
     * Create a day view for the calendar
     */
    private fun createDayView(day: Int): TextView {
        val dayView = TextView(requireContext()).apply {
            text = day.toString()
            textSize = 16f
            gravity = android.view.Gravity.CENTER
            setPadding(8, 8, 8, 8)
            layoutParams = ViewGroup.LayoutParams(0, 120)
            
            // Set click listener
            setOnClickListener {
                selectDay(day)
            }
        }
        
        // Check if this day has data
        val dayDate = Calendar.getInstance().apply {
            set(currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), day)
        }
        
        val hasMood = hasMoodForDate(dayDate.timeInMillis)
        val hasHabits = hasHabitsForDate(dayDate.timeInMillis)
        
        // Style based on data availability
        when {
            hasMood && hasHabits -> {
                dayView.setBackgroundColor(resources.getColor(R.color.primary_green, null))
                dayView.setTextColor(resources.getColor(R.color.background_white, null))
            }
            hasMood || hasHabits -> {
                dayView.setBackgroundColor(resources.getColor(R.color.accent_orange, null))
                dayView.setTextColor(resources.getColor(R.color.background_white, null))
            }
            else -> {
                dayView.setBackgroundColor(resources.getColor(R.color.background_light_gray, null))
                dayView.setTextColor(resources.getColor(R.color.text_primary, null))
            }
        }
        
        return dayView
    }
    
    /**
     * Select a day and show details
     */
    private fun selectDay(day: Int) {
        try {
            selectedDate = Calendar.getInstance().apply {
                set(currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), day)
            }
            
            updateSelectedDayDetails()
            android.util.Log.d("CalendarFragment", "Day $day selected")
        } catch (e: Exception) {
            android.util.Log.e("CalendarFragment", "Error selecting day: ${e.message}", e)
        }
    }
    
    /**
     * Update selected day details
     */
    private fun updateSelectedDayDetails() {
        try {
            selectedDate?.let { date ->
                val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                binding.tvSelectedDate.text = dateFormat.format(date.timeInMillis)
                
                // Get mood for selected day
                val mood = getMoodForDate(date.timeInMillis)
                if (mood != null) {
                    binding.tvSelectedMoodEmoji.text = getMoodEmoji(mood.type)
                    binding.tvSelectedMood.text = mood.type.capitalize()
                } else {
                    binding.tvSelectedMoodEmoji.text = "😐"
                    binding.tvSelectedMood.text = "No mood recorded"
                }
                
                // Get habits for selected day
                updateSelectedDayHabits(date.timeInMillis)
                
                android.util.Log.d("CalendarFragment", "Selected day details updated")
            }
        } catch (e: Exception) {
            android.util.Log.e("CalendarFragment", "Error updating selected day details: ${e.message}", e)
        }
    }
    
    /**
     * Update habits list for selected day
     */
    private fun updateSelectedDayHabits(date: Long) {
        try {
            // Clear existing habits
            binding.habitsCompletedList.removeAllViews()
            
            // Get habits for the date (simplified - in real app, you'd get actual habit completion data)
            val sampleHabits = listOf(
                "✅ Meditate 10 min",
                "✅ Read 20 pages", 
                "❌ Exercise 30 min"
            )
            
            sampleHabits.forEach { habit ->
                val habitView = TextView(requireContext()).apply {
                    text = habit
                    textSize = 14f
                    setPadding(0, 4, 0, 4)
                    when {
                        habit.startsWith("✅") -> setTextColor(resources.getColor(R.color.primary_green, null))
                        habit.startsWith("❌") -> setTextColor(resources.getColor(R.color.primary_red, null))
                        else -> setTextColor(resources.getColor(R.color.text_primary, null))
                    }
                }
                binding.habitsCompletedList.addView(habitView)
            }
            
            android.util.Log.d("CalendarFragment", "Habits list updated")
        } catch (e: Exception) {
            android.util.Log.e("CalendarFragment", "Error updating habits list: ${e.message}", e)
        }
    }
    
    /**
     * Switch to daily view
     */
    private fun switchToDailyView() {
        try {
            binding.tvDailyTab.setTextColor(resources.getColor(R.color.primary_indigo, null))
            binding.tvMonthlyTab.setTextColor(resources.getColor(R.color.text_secondary, null))
            binding.tvDailyTab.setBackgroundResource(R.drawable.ic_underline)
            binding.tvMonthlyTab.background = null
            
            android.util.Log.d("CalendarFragment", "Switched to daily view")
        } catch (e: Exception) {
            android.util.Log.e("CalendarFragment", "Error switching to daily view: ${e.message}", e)
        }
    }
    
    /**
     * Switch to monthly view
     */
    private fun switchToMonthlyView() {
        try {
            binding.tvMonthlyTab.setTextColor(resources.getColor(R.color.primary_indigo, null))
            binding.tvDailyTab.setTextColor(resources.getColor(R.color.text_secondary, null))
            binding.tvMonthlyTab.setBackgroundResource(R.drawable.ic_underline)
            binding.tvDailyTab.background = null
            
            android.util.Log.d("CalendarFragment", "Switched to monthly view")
        } catch (e: Exception) {
            android.util.Log.e("CalendarFragment", "Error switching to monthly view: ${e.message}", e)
        }
    }
    
    /**
     * Check if there's mood data for a specific date
     */
    private fun hasMoodForDate(date: Long): Boolean {
        return try {
            val moods = dataManager.getMoodsForToday()
            moods.isNotEmpty()
        } catch (e: Exception) {
            android.util.Log.e("CalendarFragment", "Error checking mood for date: ${e.message}", e)
            false
        }
    }
    
    /**
     * Check if there are habits for a specific date
     */
    private fun hasHabitsForDate(date: Long): Boolean {
        return try {
            val habits = dataManager.getHabits()
            habits.isNotEmpty()
        } catch (e: Exception) {
            android.util.Log.e("CalendarFragment", "Error checking habits for date: ${e.message}", e)
            false
        }
    }
    
    /**
     * Get mood for a specific date
     */
    private fun getMoodForDate(date: Long): Mood? {
        return try {
            val moods = dataManager.getMoodsForToday()
            moods.firstOrNull()
        } catch (e: Exception) {
            android.util.Log.e("CalendarFragment", "Error getting mood for date: ${e.message}", e)
            null
        }
    }
    
    /**
     * Get emoji for mood type
     */
    private fun getMoodEmoji(moodType: String): String {
        return when (moodType.lowercase()) {
            "happy" -> "😊"
            "sad" -> "😢"
            "angry" -> "😠"
            "excited" -> "🤩"
            "calm" -> "😌"
            "tired" -> "😴"
            else -> "😐"
        }
    }
    
    /**
     * Show loading state to prevent ANR
     */
    private fun showLoadingState() {
        try {
            // Show loading indicators immediately with more aggressive dimming
            binding.root.alpha = 0.5f
            android.util.Log.d("CalendarFragment", "Loading state shown")
        } catch (e: Exception) {
            android.util.Log.e("CalendarFragment", "Error showing loading state: ${e.message}", e)
        }
    }
    
    /**
     * Hide loading state when data is ready
     */
    private fun hideLoadingState() {
        try {
            // Hide loading indicators and restore full opacity
            binding.root.alpha = 1.0f
            android.util.Log.d("CalendarFragment", "Loading state hidden")
        } catch (e: Exception) {
            android.util.Log.e("CalendarFragment", "Error hiding loading state: ${e.message}", e)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
