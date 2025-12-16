package com.wellnessapp.ui.hydration

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.wellnessapp.R
import com.wellnessapp.data.DataManager
import com.wellnessapp.data.RealtimeDataManager
import com.wellnessapp.databinding.FragmentHydrationBinding

/**
 * Hydration Fragment - Track daily water intake
 * Features:
 * - Add water glasses/bottles
 * - Track daily hydration progress
 * - Set hydration goals
 * - View hydration history
 */
class HydrationFragment : Fragment() {

    private var _binding: FragmentHydrationBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var hydrationViewModel: HydrationViewModel
    private lateinit var realtimeDataManager: RealtimeDataManager
    private lateinit var dataManager: DataManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHydrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            android.util.Log.d("HydrationFragment", "onViewCreated called")
            
            // Initialize ViewModel first (lightweight)
            hydrationViewModel = ViewModelProvider(this)[HydrationViewModel::class.java]
            
            // Setup UI immediately (non-blocking)
            setupUI()
            android.util.Log.d("HydrationFragment", "UI setup completed")
            
            // Initialize heavy components in background thread
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                try {
                    // Initialize DataManager in background
                    dataManager = DataManager.getInstance(requireContext())
                    android.util.Log.d("HydrationFragment", "DataManager initialized in background")
                    
                    // Initialize RealtimeDataManager in background
                    realtimeDataManager = RealtimeDataManager.getInstance()
                    android.util.Log.d("HydrationFragment", "RealtimeDataManager initialized in background")
                    
                    // Initialize data manager in ViewModel
                    hydrationViewModel.initializeDataManager(requireContext())
                    
                    // Initialize RealtimeDataManager (heavy operation)
                    realtimeDataManager.initialize(requireContext())
                    android.util.Log.d("HydrationFragment", "RealtimeDataManager initialized in background")
                    
                    // Setup observers after heavy initialization
                    observeData()
                    observeRealtimeData()
                    android.util.Log.d("HydrationFragment", "HydrationFragment setup completed successfully")
                } catch (e: Exception) {
                    android.util.Log.e("HydrationFragment", "Error in background initialization: ${e.message}", e)
                    Toast.makeText(context, "Error loading hydration data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("HydrationFragment", "Error in onViewCreated: ${e.message}", e)
            Toast.makeText(context, "Error loading hydration page: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh settings when returning to the fragment
        refreshSettings()
    }
    
    /**
     * Refresh settings and UI when returning to the fragment
     */
    private fun refreshSettings() {
        try {
            // Update goal display
            updateGoalDisplay()
            
            // Initialize progress display
            initializeProgressDisplay()
            
            // Update interval display
            updateIntervalDisplay()
            
            // Recreate glass layouts if goal changed
            val currentGoal = dataManager.getHydrationGoal()
            createDynamicGlassLayouts(currentGoal)
            
            // Update hydration display
            updateHydrationDisplay()
            
            android.util.Log.d("HydrationFragment", "Settings refreshed on resume")
        } catch (e: Exception) {
            android.util.Log.e("HydrationFragment", "Error refreshing settings: ${e.message}", e)
        }
    }

    private fun setupUI() {
        try {
            android.util.Log.d("HydrationFragment", "Setting up UI")
            
            // Back button
            binding.btnBack?.setOnClickListener {
                requireActivity().onBackPressed()
            }
            
            // Setup individual glass checkboxes
            setupGlassCheckboxes()
            
            // Quick Actions
            setupQuickActions()
            
            // Settings
            setupSettings()
            
            // Add bottle button (removed - button doesn't exist in layout)
            
            // Toggle buttons
            binding.btnReminders?.setOnClickListener {
                toggleReminders()
            }
            
            binding.btnGoal?.setOnClickListener {
                showGoalDialog()
            }
            
            // Update hydration display
            updateHydrationDisplay()
            android.util.Log.d("HydrationFragment", "UI setup completed")
        } catch (e: Exception) {
            android.util.Log.e("HydrationFragment", "Error in setupUI: ${e.message}", e)
            Toast.makeText(context, "Error setting up hydration UI: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Setup individual glass checkboxes with click listeners
     */
    private fun setupGlassCheckboxes() {
        try {
            android.util.Log.d("HydrationFragment", "Setting up dynamic glass checkboxes")
            
            val goal = dataManager.getHydrationGoal()
            createDynamicGlassLayouts(goal)
            
            // Load current glass completion state
            loadGlassCompletionState()
            android.util.Log.d("HydrationFragment", "Dynamic glass checkboxes setup completed")
        } catch (e: Exception) {
            android.util.Log.e("HydrationFragment", "Error setting up glass checkboxes: ${e.message}", e)
        }
    }
    
    /**
     * Create dynamic glass layouts based on goal
     */
    private fun createDynamicGlassLayouts(goal: Int) {
        try {
            // Clear existing glass layouts
            binding.glassesGrid?.removeAllViews()
            
            // Create new glass layouts based on goal
            for (i in 1..goal) {
                val glassLayout = createGlassLayout(i)
                binding.glassesGrid?.addView(glassLayout)
            }
            
            android.util.Log.d("HydrationFragment", "Created $goal dynamic glass layouts")
        } catch (e: Exception) {
            android.util.Log.e("HydrationFragment", "Error creating dynamic glass layouts: ${e.message}", e)
        }
    }
    
    /**
     * Create a single glass layout
     */
    private fun createGlassLayout(glassNumber: Int): LinearLayout {
        val layout = LinearLayout(requireContext()).apply {
            id = View.generateViewId()
            orientation = LinearLayout.VERTICAL
            gravity = android.view.Gravity.CENTER
            setPadding(32, 32, 32, 32)
            background = resources.getDrawable(R.drawable.glass_background, null)
            isClickable = true
            isFocusable = true
            
            // Set layout parameters for grid
            val params = android.widget.GridLayout.LayoutParams().apply {
                width = 0
                height = android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                columnSpec = android.widget.GridLayout.spec(android.widget.GridLayout.UNDEFINED, 1f)
                setMargins(16, 16, 16, 16)
            }
            layoutParams = params
        }
        
        // Add glass icon
        val iconView = android.widget.ImageView(requireContext()).apply {
            id = View.generateViewId()
            layoutParams = android.widget.LinearLayout.LayoutParams(128, 128)
            setImageResource(R.drawable.ic_water_glass_empty)
        }
        layout.addView(iconView)
        
        // Add glass text
        val textView = android.widget.TextView(requireContext()).apply {
            id = View.generateViewId()
            text = "Glass $glassNumber"
            textSize = 12f
            setTextColor(resources.getColor(R.color.text_secondary, null))
            gravity = android.view.Gravity.CENTER
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 16
            }
        }
        layout.addView(textView)
        
        // Set click listener
        layout.setOnClickListener {
            if (dataManager.getGlassCompletion(glassNumber)) {
                markGlassIncomplete(glassNumber)
            } else {
                markGlassCompleted(glassNumber)
            }
        }
        
        return layout
    }
    
    /**
     * Mark a specific glass as completed
     */
    private fun markGlassCompleted(glassNumber: Int) {
        try {
            val goal = dataManager.getHydrationGoal()
            val currentCompleted = countCompletedGlasses()
            
            // Check if we can complete this glass (don't exceed goal)
            if (currentCompleted >= goal) {
                Toast.makeText(requireContext(), "Goal already reached! ($goal glasses)", Toast.LENGTH_SHORT).show()
                return
            }
            
            // Mark this specific glass as completed
            dataManager.setGlassCompletion(glassNumber, true)
            
            // Count total completed glasses
            val totalCompleted = countCompletedGlasses()
            
            // Ensure we don't exceed the goal
            val finalCompleted = minOf(totalCompleted, goal)
            
            // Save total hydration count
            dataManager.saveHydration(finalCompleted)
            
            // Update real-time data manager for home page sync
            realtimeDataManager.updateHydration(finalCompleted)
            realtimeDataManager.updateHydrationLevel(finalCompleted)
            
            // Update progress with smooth animation
            updateHydrationProgress(finalCompleted)
            
            // Show enhanced feedback with animation
            val feedbackMessages = listOf(
                "Glass $glassNumber completed! 💧",
                "Great job! Glass $glassNumber done! 🌊",
                "Hydration level up! Glass $glassNumber ✅",
                "Keep it up! Glass $glassNumber completed! 💪"
            )
            val randomMessage = feedbackMessages.random()
            Toast.makeText(requireContext(), randomMessage, Toast.LENGTH_SHORT).show()
            
            // Add haptic feedback
            try {
                val vibrator = requireContext().getSystemService(android.content.Context.VIBRATOR_SERVICE) as android.os.Vibrator
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator.vibrate(android.os.VibrationEffect.createOneShot(100, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(100)
                }
            } catch (e: Exception) {
                android.util.Log.e("HydrationFragment", "Error with haptic feedback: ${e.message}")
            }
            
            // Update display
            updateHydrationDisplay()
            updateGlassAppearance(glassNumber, true)
            
            android.util.Log.d("HydrationFragment", "Glass $glassNumber completed. Total: $finalCompleted/$goal glasses")
        } catch (e: Exception) {
            android.util.Log.e("HydrationFragment", "Error marking glass $glassNumber as completed: ${e.message}", e)
            Toast.makeText(context, "Error updating glass completion: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Mark a specific glass as incomplete
     */
    private fun markGlassIncomplete(glassNumber: Int) {
        try {
            // Mark this specific glass as incomplete
            dataManager.setGlassCompletion(glassNumber, false)
            
            // Count total completed glasses
            val totalCompleted = countCompletedGlasses()
            
            // Save total hydration count directly
            dataManager.saveHydration(totalCompleted)
            
            // Update real-time data manager for home page sync
            realtimeDataManager.updateHydration(totalCompleted)
            realtimeDataManager.updateHydrationLevel(totalCompleted)
            
            // Show feedback
            Toast.makeText(requireContext(), "Glass $glassNumber unchecked", Toast.LENGTH_SHORT).show()
            
            // Update display
            updateHydrationDisplay()
            updateGlassCheckboxes(totalCompleted)
            
            android.util.Log.d("HydrationFragment", "Glass $glassNumber unchecked. Total: $totalCompleted glasses")
        } catch (e: Exception) {
            android.util.Log.e("HydrationFragment", "Error marking glass $glassNumber as incomplete: ${e.message}", e)
            Toast.makeText(context, "Error updating glass completion: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Update glass appearance (indigo when completed)
     */
    private fun updateGlassAppearance(glassNumber: Int, isCompleted: Boolean) {
        try {
            // Find the glass layout by glass number
            val glassLayout = findGlassLayout(glassNumber)
            glassLayout?.let { layout ->
                if (isCompleted) {
                    // Set indigo background for completed glass
                    layout.setBackgroundColor(resources.getColor(R.color.primary_indigo, null))
                    // Update text color to white
                    val textView = layout.getChildAt(1) as? android.widget.TextView
                    textView?.setTextColor(resources.getColor(R.color.white, null))
                } else {
                    // Reset to default appearance
                    layout.background = resources.getDrawable(R.drawable.glass_background, null)
                    val textView = layout.getChildAt(1) as? android.widget.TextView
                    textView?.setTextColor(resources.getColor(R.color.text_secondary, null))
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("HydrationFragment", "Error updating glass appearance: ${e.message}", e)
        }
    }
    
    /**
     * Find glass layout by glass number
     */
    private fun findGlassLayout(glassNumber: Int): LinearLayout? {
        try {
            val gridLayout = binding.glassesGrid
            for (i in 0 until gridLayout.childCount) {
                val child = gridLayout.getChildAt(i)
                if (child is LinearLayout) {
                    val textView = child.getChildAt(1) as? android.widget.TextView
                    if (textView?.text == "Glass $glassNumber") {
                        return child
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("HydrationFragment", "Error finding glass layout: ${e.message}", e)
        }
        return null
    }
    
    /**
     * Count total completed glasses
     */
    private fun countCompletedGlasses(): Int {
        var count = 0
        val goal = dataManager.getHydrationGoal()
        for (i in 1..goal) {
            if (dataManager.getGlassCompletion(i)) {
                count++
            }
        }
        return count
    }
    
    /**
     * Load current glass completion state from data
     */
    private fun loadGlassCompletionState() {
        try {
            val goal = dataManager.getHydrationGoal()
            
            // Update each glass layout based on completion state
            for (i in 1..goal) {
                val isCompleted = dataManager.getGlassCompletion(i)
                updateGlassAppearance(i, isCompleted)
            }
            
            android.util.Log.d("HydrationFragment", "Loaded glass completion state for $goal glasses")
        } catch (e: Exception) {
            android.util.Log.e("HydrationFragment", "Error loading glass completion state: ${e.message}", e)
        }
    }
    
    /**
     * Update the visual appearance of a glass based on completion status
     */
    private fun updateGlassAppearance(layout: LinearLayout?, isCompleted: Boolean) {
        layout?.let {
            if (isCompleted) {
                it.background = resources.getDrawable(R.drawable.glass_background_completed, null)
            } else {
                it.background = resources.getDrawable(R.drawable.glass_background, null)
            }
        }
    }

    private fun observeData() {
        // Hydration data is updated directly when user adds water
        // No need to observe changes as we update display immediately
    }
    
    /**
     * Observe real-time data changes
     */
    private fun observeRealtimeData() {
        realtimeDataManager.hydrationProgress.observe(viewLifecycleOwner) { glasses ->
            updateHydrationProgress(glasses)
        }
    }
    
    /**
     * Update hydration progress with real-time data
     */
    private fun updateHydrationProgress(glasses: Int) {
        try {
            val goal = dataManager.getHydrationGoal()
            val progressPercentage = (glasses * 100) / goal
            val progressValue = (glasses * 100) / goal
            
            // Update progress bar with smooth animation
            binding.progressHydration?.let { progressBar ->
                progressBar.max = 100
                progressBar.progress = progressValue
                
                // Add smooth animation
                val animator = android.animation.ObjectAnimator.ofInt(progressBar, "progress", progressBar.progress, progressValue)
                animator.duration = 500
                animator.start()
            }
            
            // Update progress text with enhanced formatting
            binding.tvHydrationProgress?.text = "${glasses}/${goal} glasses"
            
            // Update liters progress display (assuming 1 glass = 0.25L)
            val currentLiters = glasses * 0.25
            val goalLiters = goal * 0.25
            binding.tvLitersProgress?.text = "${String.format("%.1f", currentLiters)}L / ${String.format("%.1f", goalLiters)}L"
            
            // Update motivational message based on progress
            updateMotivationalMessage(glasses, goal)
            
            // Update glass visual states
            updateGlassVisualStates(glasses, goal)
            
            android.util.Log.d("HydrationFragment", "Updated progress: $glasses/$goal glasses ($progressPercentage%)")
        } catch (e: Exception) {
            android.util.Log.e("HydrationFragment", "Error updating hydration progress: ${e.message}", e)
        }
    }
    
    /**
     * Update motivational message based on hydration progress
     */
    private fun updateMotivationalMessage(glasses: Int, goal: Int) {
        val message = when {
            glasses == 0 -> "💧 Start your hydration journey!"
            glasses < goal / 4 -> "🌊 Keep going! Every drop counts!"
            glasses < goal / 2 -> "💪 You're doing great! Keep hydrating!"
            glasses < (goal * 3) / 4 -> "🔥 Almost there! You're on fire!"
            glasses >= goal -> "🎉 Goal achieved! You're a hydration champion!"
            else -> "💪 Keep hydrating for better health!"
        }
        
        binding.tvMotivationalMessage?.text = message
    }
    
    /**
     * Update glass visual states based on current progress
     */
    private fun updateGlassVisualStates(glasses: Int, goal: Int) {
        try {
            // Update each glass in the grid
            for (i in 1..goal) {
                val glassLayout = binding.glassesGrid?.findViewById<LinearLayout>(getGlassId(i))
                val isCompleted = i <= glasses
                updateGlassAppearance(glassLayout, isCompleted)
            }
        } catch (e: Exception) {
            android.util.Log.e("HydrationFragment", "Error updating glass visual states: ${e.message}", e)
        }
    }
    
    /**
     * Get glass ID for the grid
     */
    private fun getGlassId(glassNumber: Int): Int {
        return when (glassNumber) {
            1 -> R.id.glass_1
            2 -> R.id.glass_2
            3 -> R.id.glass_3
            4 -> R.id.glass_4
            5 -> R.id.glass_5
            6 -> R.id.glass_6
            7 -> R.id.glass_7
            8 -> R.id.glass_8
            else -> R.id.glass_1
        }
    }
    
    /**
     * Initialize progress display with current data
     */
    private fun initializeProgressDisplay() {
        try {
            val currentHydration = dataManager.getHydration()
            val goal = dataManager.getHydrationGoal()
            
            // Update progress bar
            val progressPercentage = (currentHydration * 100) / goal
            binding.progressHydration?.let { progressBar ->
                progressBar.max = 100
                progressBar.progress = progressPercentage
            }
            
            // Update progress text
            binding.tvHydrationProgress?.text = "$currentHydration/$goal glasses"
            
            // Update liters progress display
            val currentLiters = currentHydration * 0.25
            val goalLiters = goal * 0.25
            binding.tvLitersProgress?.text = "${String.format("%.1f", currentLiters)}L / ${String.format("%.1f", goalLiters)}L"
            
            // Update motivational message
            updateMotivationalMessageForCurrent(currentHydration, goal)
            
            // Update glass visual states
            updateGlassVisualStates(currentHydration, goal)
            
            android.util.Log.d("HydrationFragment", "Initialized progress display: $currentHydration/$goal glasses")
        } catch (e: Exception) {
            android.util.Log.e("HydrationFragment", "Error initializing progress display: ${e.message}", e)
        }
    }

    private fun addWater(glasses: Int) {
        try {
            val currentHydration = dataManager.getHydration()
            val newHydration = currentHydration + glasses
            dataManager.saveHydration(newHydration)
            
            // Update real-time data manager for home page sync
            realtimeDataManager.updateHydration(newHydration)
            realtimeDataManager.updateHydrationLevel(newHydration)
            
            // Update progress with smooth animation
            updateHydrationProgress(newHydration)
            
            // Update glass checkboxes to reflect the new hydration level
            updateGlassCheckboxes(newHydration)
            
            // Show feedback
            val message = if (glasses == 1) "Added 1 glass of water! 💧" else "Added 1 bottle of water! 💧"
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            
            // Update display
            updateHydrationDisplay()
            
            android.util.Log.d("HydrationFragment", "Added $glasses glasses. Total: $newHydration")
        } catch (e: Exception) {
            android.util.Log.e("HydrationFragment", "Error adding water: ${e.message}", e)
            Toast.makeText(context, "Error adding water: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateHydrationDisplay() {
        try {
            android.util.Log.d("HydrationFragment", "Updating hydration display")
            val goal = dataManager.getHydrationGoal()
            
            // Use countCompletedGlasses() to get accurate count that respects goal
            val hydration = countCompletedGlasses()
            
            // Ensure hydration never exceeds goal
            val finalHydration = minOf(hydration, goal)
            
            android.util.Log.d("HydrationFragment", "Hydration data: $finalHydration glasses, goal: $goal")
            
            // Update progress text
            if (binding.tvHydrationProgress != null) {
                binding.tvHydrationProgress.text = "$finalHydration / $goal glasses"
                android.util.Log.d("HydrationFragment", "Updated glasses drank text")
            } else {
                android.util.Log.e("HydrationFragment", "tvHydrationProgress is null")
            }
            
            // Update percentage text - using progress bar instead
            val percentage = if (goal > 0) (finalHydration.toFloat() / goal * 100).toInt() else 0
            android.util.Log.d("HydrationFragment", "Hydration percentage: $percentage%")
            
            // Update progress bar with animation
            val progress = if (goal > 0) (finalHydration.toFloat() / goal * 100).toInt() else 0
            if (binding.progressHydration != null) {
                binding.progressHydration.max = 100
                binding.progressHydration.progress = progress
                // Animate progress bar
                binding.progressHydration.animate()
                    .setDuration(500)
                    .setStartDelay(100)
                    .start()
                binding.progressHydration.progress = progress
                android.util.Log.d("HydrationFragment", "Updated progress bar: $progress%")
            } else {
                android.util.Log.e("HydrationFragment", "progressHydration is null")
            }
            
            // Update motivational message
            updateMotivationalMessageForCurrent(hydration, goal)
            
            // Update summary - using goal text instead
            val remaining = maxOf(0, goal - hydration)
            android.util.Log.d("HydrationFragment", "Remaining glasses: $remaining")
            
            // Update glass checkboxes based on current hydration
            updateGlassCheckboxes(hydration)
            
            android.util.Log.d("HydrationFragment", "Hydration display updated successfully")
        } catch (e: Exception) {
            android.util.Log.e("HydrationFragment", "Error updating hydration display: ${e.message}", e)
            Toast.makeText(context, "Error updating hydration display: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun updateMotivationalMessageForCurrent(current: Int, goal: Int) {
        try {
            val messages = when {
                current == 0 -> listOf(
                    "💧 Start your hydration journey!",
                    "🌅 Begin your day with water!",
                    "💪 First glass is the hardest!"
                )
                current < goal / 2 -> listOf(
                    "💧 Great start! Keep going!",
                    "🌊 You're building a healthy habit!",
                    "💪 Every glass counts!"
                )
                current < goal -> listOf(
                    "💧 You're halfway there!",
                    "🌊 Keep up the great work!",
                    "💪 Almost at your goal!"
                )
                current >= goal -> listOf(
                    "🎉 Goal achieved! You're amazing!",
                    "💧 Perfect hydration today!",
                    "🌟 You're a hydration champion!"
                )
                else -> listOf("💧 Keep hydrating for better health!")
            }
            
            val randomMessage = messages.random()
            binding.tvMotivationalMessage?.text = randomMessage
            
            android.util.Log.d("HydrationFragment", "Motivational message updated: $randomMessage")
        } catch (e: Exception) {
            android.util.Log.e("HydrationFragment", "Error updating motivational message: ${e.message}", e)
        }
    }
    
    /**
     * Update glass checkboxes based on current hydration level
     */
    private fun updateGlassCheckboxes(currentHydration: Int) {
        try {
            val glassLayouts = listOf(
                binding.glass1,
                binding.glass2,
                binding.glass3,
                binding.glass4,
                binding.glass5,
                binding.glass6,
                binding.glass7,
                binding.glass8
            )
            
            glassLayouts.forEachIndexed { index, layout ->
                val isCompleted = dataManager.getGlassCompletion(index + 1)
                updateGlassAppearance(layout, isCompleted)
            }
            
            android.util.Log.d("HydrationFragment", "Updated glass checkboxes for $currentHydration glasses")
        } catch (e: Exception) {
            android.util.Log.e("HydrationFragment", "Error updating glass checkboxes: ${e.message}", e)
        }
    }

    private fun toggleReminders() {
        // Run in background to prevent UI lag
        android.os.Handler(android.os.Looper.getMainLooper()).post {
            try {
                val isEnabled = dataManager.getReminderEnabled()
                val newState = !isEnabled
                dataManager.setReminderEnabled(newState)
                
                // Also save to wellness_app SharedPreferences for consistency
                val sharedPrefs = requireContext().getSharedPreferences("wellness_app", Context.MODE_PRIVATE)
                sharedPrefs.edit()
                    .putBoolean("hydration_reminders_enabled", newState)
                    .apply()
                
                // Schedule or cancel reminders using WorkManager
                val notificationManager = com.wellnessapp.notifications.HydrationNotificationManager(requireContext())
                if (newState) {
                    notificationManager.scheduleHydrationReminders()
                } else {
                    notificationManager.cancelHydrationReminders()
                }
                
                val message = if (newState) "Hydration reminders enabled" else "Hydration reminders disabled"
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                
                // Update button appearance
                binding.switchReminders?.isChecked = newState
                
                // Change button color to orange when activated
                if (newState) {
                    binding.btnReminders?.setBackgroundColor(resources.getColor(R.color.accent_orange, null))
                    binding.btnReminders?.setTextColor(resources.getColor(R.color.white, null))
                } else {
                    binding.btnReminders?.setBackgroundColor(resources.getColor(R.color.background_light_gray, null))
                    binding.btnReminders?.setTextColor(resources.getColor(R.color.text_primary, null))
                }
                
                android.util.Log.d("HydrationFragment", "Reminders toggled: $newState")
            } catch (e: Exception) {
                android.util.Log.e("HydrationFragment", "Error toggling reminders: ${e.message}", e)
                Toast.makeText(context, "Error toggling reminders: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showGoalDialog() {
        try {
            val currentGoal = dataManager.getHydrationGoal()
            val builder = android.app.AlertDialog.Builder(requireContext())
            builder.setTitle("Set Hydration Goal")
            builder.setMessage("How many glasses of water do you want to drink daily?")
            
            val input = android.widget.EditText(requireContext())
            input.inputType = android.text.InputType.TYPE_CLASS_NUMBER
            input.setText(currentGoal.toString())
            builder.setView(input)
            
            builder.setPositiveButton("Set Goal") { _, _ ->
                try {
                    val newGoal = input.text.toString().toIntOrNull() ?: 8
                    if (newGoal > 0 && newGoal <= 20) {
                        dataManager.setHydrationGoal(newGoal)
                        // Recreate glass layouts with new goal
                        createDynamicGlassLayouts(newGoal)
                        updateHydrationDisplay()
                        loadGlassCompletionState()
                        Toast.makeText(requireContext(), "Goal set to $newGoal glasses", Toast.LENGTH_SHORT).show()
                        android.util.Log.d("HydrationFragment", "Hydration goal set to: $newGoal")
                    } else {
                        Toast.makeText(requireContext(), "Please enter a goal between 1 and 20 glasses", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("HydrationFragment", "Error setting goal: ${e.message}", e)
                    Toast.makeText(context, "Error setting goal: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            
            builder.setNegativeButton("Cancel", null)
            builder.show()
        } catch (e: Exception) {
            android.util.Log.e("HydrationFragment", "Error showing goal dialog: ${e.message}", e)
            Toast.makeText(context, "Error showing goal dialog: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun setupQuickActions() {
        // Quick actions setup (buttons don't exist in layout)
        android.util.Log.d("HydrationFragment", "Quick actions setup completed")
        
        // Reminders button
        binding.btnReminders?.setOnClickListener {
            showReminderSettings()
        }
        
        // Goal button
        binding.btnGoal?.setOnClickListener {
            showGoalDialog()
        }
    }
    
    private fun setupSettings() {
        // Goal setting click
        binding.tvGoalValue?.setOnClickListener {
            showGoalDialog()
        }
        
        // Reminder toggle
        binding.switchReminders?.setOnCheckedChangeListener { _, isChecked ->
            toggleReminders(isChecked)
        }
        
        // Interval setting click
        binding.tvIntervalValue?.setOnClickListener {
            showIntervalDialog()
        }
    }
    
    private fun showReminderSettings() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Reminder Settings")
            .setMessage("Manage your hydration reminders")
            .setPositiveButton("Settings") { _, _ ->
                // Navigate to settings or show reminder options
                showIntervalDialog()
            }
            .setNegativeButton("Close", null)
            .show()
    }
    
    private fun showIntervalDialog() {
        val intervals = arrayOf("1 hour", "2 hours", "3 hours", "4 hours", "6 hours")
        val currentInterval = dataManager.getReminderInterval()
        val currentIndex = when (currentInterval) {
            1 -> 0
            2 -> 1
            3 -> 2
            4 -> 3
            6 -> 4
            else -> 1
        }
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Reminder Interval")
            .setSingleChoiceItems(intervals, currentIndex) { dialog, which ->
                val newInterval = when (which) {
                    0 -> 1
                    1 -> 2
                    2 -> 3
                    3 -> 4
                    4 -> 6
                    else -> 2
                }
                dataManager.setReminderInterval(newInterval)
                updateIntervalDisplay()
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun toggleReminders(enabled: Boolean) {
        dataManager.setReminderEnabled(enabled)
        if (enabled) {
            // Start reminders
            startHydrationReminders()
            Toast.makeText(context, "Hydration reminders enabled", Toast.LENGTH_SHORT).show()
        } else {
            // Stop reminders
            stopHydrationReminders()
            Toast.makeText(context, "Hydration reminders disabled", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun updateGoalDisplay() {
        val goal = dataManager.getHydrationGoal()
        binding.tvGoalValue?.text = "$goal glasses"
        binding.tvHydrationGoal?.text = "Goal: $goal glasses today"
    }
    
    private fun updateIntervalDisplay() {
        val interval = dataManager.getReminderInterval()
        binding.tvIntervalValue?.text = "$interval hours"
    }
    
    private fun updateGlassCheckboxes() {
        val goal = dataManager.getHydrationGoal()
        val currentGlasses = dataManager.getHydration()
        
        // Update progress bar
        binding.progressHydration?.max = goal
        binding.progressHydration?.progress = currentGlasses
        
        // Update progress text
        binding.tvHydrationProgress?.text = "$currentGlasses/$goal glasses"
        
        // Update individual glass checkboxes
        for (i in 1..8) {
            val glassView = when (i) {
                1 -> binding.glass1
                2 -> binding.glass2
                3 -> binding.glass3
                4 -> binding.glass4
                5 -> binding.glass5
                6 -> binding.glass6
                7 -> binding.glass7
                8 -> binding.glass8
                else -> null
            }
            
            if (glassView != null) {
                val isCompleted = i <= currentGlasses
                val isVisible = i <= goal
                
                glassView.visibility = if (isVisible) View.VISIBLE else View.GONE
                
                if (isCompleted) {
                    glassView.setBackgroundResource(R.drawable.glass_background_completed)
                } else {
                    glassView.setBackgroundResource(R.drawable.glass_background)
                }
            }
        }
    }
    
    private fun startHydrationReminders() {
        // Implementation for starting hydration reminders
        // This would integrate with the notification system
    }
    
    private fun stopHydrationReminders() {
        // Implementation for stopping hydration reminders
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
