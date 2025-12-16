package com.wellnessapp.ui.home

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.wellnessapp.R
import com.wellnessapp.data.DataManager
import com.wellnessapp.data.RealtimeDataManager
import com.wellnessapp.databinding.FragmentHomeBinding
import com.wellnessapp.ui.habits.HabitDetailDialog
import com.wellnessapp.data.Mood
import com.wellnessapp.ui.mood.MoodDetailActivity
import java.util.Date

/**
 * Home Fragment - Main dashboard showing user's wellness overview
 * Displays progress, habits, hydration, and mood tracking
 */
class HomeFragment : Fragment(), SensorEventListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var homeViewModel: HomeViewModel
    private lateinit var dataManager: DataManager
    private lateinit var realtimeDataManager: RealtimeDataManager
    
    // Sensor-related variables
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var stepCounter: Sensor? = null
    private var stepDetector: Sensor? = null
    private var vibrator: Vibrator? = null
    
    // Shake detection variables
    private var lastShakeTime = 0L
    private val shakeTimeThreshold = 1000L // 1 second between shakes
    private val shakeThreshold = 15.0f // Acceleration threshold for shake detection
    
    // Step tracking variables
    private var stepCount = 0
    private var stepCountOffset = 0
    
    // Mood tracking
    private var lastSelectedMood: Mood? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            android.util.Log.d("HomeFragment", "onViewCreated started")

            // Initialize ViewModel (lightweight)
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
            android.util.Log.d("HomeFragment", "ViewModel initialized")
        
            // Initialize real-time data manager (lightweight)
        realtimeDataManager = RealtimeDataManager.getInstance()
            android.util.Log.d("HomeFragment", "RealtimeDataManager initialized")

            // Setup basic UI immediately (non-blocking)
            setupUI()
            android.util.Log.d("HomeFragment", "Basic UI setup completed")
            
            // Initialize heavy components in background thread with delay
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                try {
                    // Check if fragment is still attached
                    if (!isAdded || isDetached) {
                        android.util.Log.w("HomeFragment", "Fragment not attached, skipping background initialization")
                        return@postDelayed
                    }

                    // Initialize data manager (heavy operation)
                    dataManager = DataManager.getInstance(requireContext())
            realtimeDataManager.initialize(requireContext())
                    android.util.Log.d("HomeFragment", "Data managers initialized in background")

                    // Check if fragment is still attached before continuing
                    if (!isAdded || isDetached) {
                        android.util.Log.w("HomeFragment", "Fragment detached during initialization")
                        return@postDelayed
                    }

                    // Setup observers and data after heavy initialization
                    setupObservers()
                    setupRealtimeData()
                    android.util.Log.d("HomeFragment", "Observers and realtime data setup completed")

                    // Setup UI components that depend on data
                    setupMoodButtons()
                    setupSeeMoreMoodButton()
                    setupProfileClick()
                    setupAddHabitButton()
                    setupHydrationCard()
                    updateGreeting()
                    updateProgressDisplay()
                    updateHydrationDisplay()
                    updateHabitCountDisplay()
                    updateStepsDisplay()
                    
                    // Refresh habit progress for real-time updates
                    realtimeDataManager.refreshHabitProgress()
                    android.util.Log.d("HomeFragment", "UI components setup completed")
                    
                    android.util.Log.d("HomeFragment", "Background initialization completed successfully")
                } catch (e: Exception) {
                    android.util.Log.e("HomeFragment", "Error in background initialization: ${e.message}", e)
                }
            }, 1000) // 1 second delay to ensure UI is fully rendered
            
            // Initialize sensor manager (lightweight, can be done on main thread)
            try {
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        stepCounter = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        stepDetector = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)
        vibrator = requireActivity().getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                android.util.Log.d("HomeFragment", "Sensors initialized")
            } catch (e: Exception) {
                android.util.Log.e("HomeFragment", "Error initializing sensors: ${e.message}", e)
            }

            android.util.Log.d("HomeFragment", "onViewCreated completed successfully")
        } catch (e: Exception) {
            android.util.Log.e("HomeFragment", "Error in onViewCreated: ${e.message}", e)
            android.util.Log.e("HomeFragment", "Stack trace: ${e.stackTraceToString()}")
        }
    }
    
    /**
     * Setup basic UI components
     */
    private fun setupUI() {
        try {
            // Setup basic UI elements that don't require heavy data operations
            binding.apply {
                // Initialize basic UI components here
                android.util.Log.d("HomeFragment", "Basic UI components initialized")
            }
        } catch (e: Exception) {
            android.util.Log.e("HomeFragment", "Error in setupUI: ${e.message}", e)
        }
    }
    
    /**
     * Setup observers for real-time data updates
     */
    private fun setupObservers() {
        try {
            // Setup observers for real-time data
            realtimeDataManager.hydrationProgress.observe(viewLifecycleOwner) { glasses ->
                updateHydrationDisplay(glasses)
            }
            
            realtimeDataManager.stepCount.observe(viewLifecycleOwner) { steps ->
                updateStepsDisplay(steps)
            }
            
            // Observe habit progress changes for real-time updates
            realtimeDataManager.habitProgress.observe(viewLifecycleOwner) { progress ->
                updateProgressDisplay()
                updateHabitCountDisplay()
                android.util.Log.d("HomeFragment", "Habit progress updated: $progress")
            }
            
            android.util.Log.d("HomeFragment", "Observers setup completed")
        } catch (e: Exception) {
            android.util.Log.e("HomeFragment", "Error setting up observers: ${e.message}", e)
        }
    }

    /**
     * Setup real-time data simulation
     */
    private fun setupRealtimeData() {
        try {
            // Start real-time data simulation
        realtimeDataManager.startRealtimeSimulation()
            android.util.Log.d("HomeFragment", "Real-time data simulation started")
        } catch (e: Exception) {
            android.util.Log.e("HomeFragment", "Error starting real-time data: ${e.message}", e)
        }
    }

    /**
     * Setup mood buttons
     */
    private fun setupMoodButtons() {
        try {
            binding.apply {
                btnMoodHappy?.setOnClickListener { selectMood("happy", "😊") }
                btnMoodSad?.setOnClickListener { selectMood("sad", "😢") }
                btnMoodAngry?.setOnClickListener { selectMood("angry", "😠") }
                btnMoodTired?.setOnClickListener { selectMood("tired", "😴") }
                btnMoodWorried?.setOnClickListener { selectMood("worried", "😰") }
                btnMoodNeutral?.setOnClickListener { selectMood("neutral", "😐") }
            }
            android.util.Log.d("HomeFragment", "Mood buttons setup completed")
        } catch (e: Exception) {
            android.util.Log.e("HomeFragment", "Error setting up mood buttons: ${e.message}", e)
        }
    }
    
    /**
     * Setup see more mood button
     */
    private fun setupSeeMoreMoodButton() {
        try {
            binding.btnSeeMoreMood?.setOnClickListener {
                try {
                    val intent = android.content.Intent(requireContext(), MoodDetailActivity::class.java)
                    startActivity(intent)
                } catch (e: Exception) {
                    android.util.Log.e("HomeFragment", "Error opening mood detail: ${e.message}", e)
                }
            }
            android.util.Log.d("HomeFragment", "See more mood button setup completed")
        } catch (e: Exception) {
            android.util.Log.e("HomeFragment", "Error setting up see more mood button: ${e.message}", e)
        }
    }
    
    /**
     * Setup profile click handler
     */
    private fun setupProfileClick() {
        try {
            // Profile navigation will be handled by bottom navigation
            android.util.Log.d("HomeFragment", "Profile click setup completed")
        } catch (e: Exception) {
            android.util.Log.e("HomeFragment", "Error setting up profile click: ${e.message}", e)
        }
    }
    
    /**
     * Setup add habit button
     */
    private fun setupAddHabitButton() {
        try {
            binding.cardHabits?.setOnClickListener {
                try {
                    // Navigate to habits page instead of opening dialog
                    findNavController().navigate(R.id.nav_habits)
                    android.util.Log.d("HomeFragment", "Navigated to habits page")
                } catch (e: Exception) {
                    android.util.Log.e("HomeFragment", "Error navigating to habits page: ${e.message}", e)
                }
            }
            android.util.Log.d("HomeFragment", "Add habit button setup completed")
        } catch (e: Exception) {
            android.util.Log.e("HomeFragment", "Error setting up add habit button: ${e.message}", e)
        }
    }
    
    /**
     * Setup hydration card click handler
     */
    private fun setupHydrationCard() {
        try {
            binding.cardHydration?.setOnClickListener {
                try {
                    // Navigate to hydration page using bottom navigation
                    findNavController().navigate(R.id.nav_hydration)
                    android.util.Log.d("HomeFragment", "Navigated to hydration page")
                } catch (e: Exception) {
                    android.util.Log.e("HomeFragment", "Error navigating to hydration page: ${e.message}", e)
                }
            }
            android.util.Log.d("HomeFragment", "Hydration card setup completed")
        } catch (e: Exception) {
            android.util.Log.e("HomeFragment", "Error setting up hydration card: ${e.message}", e)
        }
    }

    /**
     * Open habit detail dialog
     */
    private fun openHabitDetailDialog() {
        try {
            val dialog = HabitDetailDialog()
            dialog.show(parentFragmentManager, "HabitDetailDialog")
            android.util.Log.d("HomeFragment", "Habit detail dialog opened")
        } catch (e: Exception) {
            android.util.Log.e("HomeFragment", "Error opening habit dialog: ${e.message}", e)
        }
    }

    /**
     * Select mood
     */
    private fun selectMood(moodId: String, emoji: String) {
        try {
            lastSelectedMood = Mood(id = moodId, emoji = emoji)
            Toast.makeText(context, "Mood selected: $emoji", Toast.LENGTH_SHORT).show()
            android.util.Log.d("HomeFragment", "Mood selected: $moodId")
        } catch (e: Exception) {
            android.util.Log.e("HomeFragment", "Error selecting mood: ${e.message}", e)
        }
    }

    /**
     * Update greeting message
     */
    private fun updateGreeting() {
        try {
            binding.tvGreeting?.text = "Love and Accept Yourself"
            android.util.Log.d("HomeFragment", "Greeting updated")
        } catch (e: Exception) {
            android.util.Log.e("HomeFragment", "Error updating greeting: ${e.message}", e)
        }
    }
    
    /**
     * Update progress display with real habit data
     */
    private fun updateProgressDisplay() {
        try {
            // Get real habit progress from data manager
            val habits = dataManager.getHabits()
            val completedToday = habits.count { habit ->
                habit.isCompletedForDate(Date())
            }
            val totalHabits = habits.size
            val progressPercentage = if (totalHabits > 0) (completedToday * 100) / totalHabits else 0
            
            // Update circular progress bar
            binding.progressCircular?.progress = progressPercentage
            binding.tvHabitsCompleted?.text = "$completedToday/$totalHabits Habits Done"
            
            android.util.Log.d("HomeFragment", "Progress display updated: $completedToday/$totalHabits habits ($progressPercentage%)")
        } catch (e: Exception) {
            android.util.Log.e("HomeFragment", "Error updating progress display: ${e.message}", e)
        }
    }
    
    /**
     * Update hydration display
     */
    private fun updateHydrationDisplay(glasses: Int = 0) {
        try {
            binding.tvHydrationProgress?.text = "$glasses/8 glasses"
            binding.progressHydration?.progress = (glasses * 100) / 8
            android.util.Log.d("HomeFragment", "Hydration display updated: $glasses glasses")
        } catch (e: Exception) {
            android.util.Log.e("HomeFragment", "Error updating hydration display: ${e.message}", e)
        }
    }
    
    /**
     * Update steps display
     */
    private fun updateStepsDisplay(steps: Int = 0) {
        try {
            binding.tvStepsCount?.text = "${steps.formatWithCommas()} / 10,000"
            android.util.Log.d("HomeFragment", "Steps display updated: $steps steps")
        } catch (e: Exception) {
            android.util.Log.e("HomeFragment", "Error updating steps display: ${e.message}", e)
        }
    }
    
    /**
     * Update habit count display with real data
     */
    private fun updateHabitCountDisplay(count: Int = 0) {
        try {
            // Get real habit data
            val habits = dataManager.getHabits()
            val completedToday = habits.count { habit ->
                habit.isCompletedForDate(Date())
            }
            val totalHabits = habits.size
            
            binding.tvHabitsProgress?.text = "$completedToday/$totalHabits Habits"
            binding.progressHabits?.progress = if (totalHabits > 0) (completedToday * 100) / totalHabits else 0
            android.util.Log.d("HomeFragment", "Habit count display updated: $completedToday/$totalHabits habits")
        } catch (e: Exception) {
            android.util.Log.e("HomeFragment", "Error updating habit count display: ${e.message}", e)
        }
    }
    
    /**
     * Log mood entry
     */
    private fun logMood(mood: Mood) {
        try {
            // Log mood entry
            android.util.Log.d("HomeFragment", "Mood logged: ${mood.id}")
        } catch (e: Exception) {
            android.util.Log.e("HomeFragment", "Error logging mood: ${e.message}", e)
        }
    }
    
    override fun onResume() {
        super.onResume()
        try {
            // Register sensor listeners
            accelerometer?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
            stepCounter?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
            stepDetector?.let { sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
            android.util.Log.d("HomeFragment", "Sensor listeners registered")
            
            // Refresh habit progress when returning to home page
            realtimeDataManager.refreshHabitProgress()
            android.util.Log.d("HomeFragment", "Habit progress refreshed on resume")
        } catch (e: Exception) {
            android.util.Log.e("HomeFragment", "Error in onResume: ${e.message}", e)
        }
    }

    override fun onPause() {
        super.onPause()
        try {
            // Unregister sensor listeners
        sensorManager.unregisterListener(this)
            android.util.Log.d("HomeFragment", "Sensor listeners unregistered")
        } catch (e: Exception) {
            android.util.Log.e("HomeFragment", "Error unregistering sensor listeners: ${e.message}", e)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        try {
            event?.let { sensorEvent ->
                when (sensorEvent.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                        // Calculate acceleration magnitude
                        val x = sensorEvent.values[0]
                        val y = sensorEvent.values[1]
                        val z = sensorEvent.values[2]
                        val acceleration = Math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            
                // Check if acceleration exceeds shake threshold
                if (acceleration > shakeThreshold) {
                val currentTime = System.currentTimeMillis()
                    // Prevent multiple shake detections within time threshold
                    if (currentTime - lastShakeTime > shakeTimeThreshold) {
                    lastShakeTime = currentTime
                        handleShake()  // Process the shake event
                    }
                }
            }
            Sensor.TYPE_STEP_COUNTER -> {
                // Get total step count since device boot
                        val totalSteps = sensorEvent.values[0].toInt()
                // Set offset on first reading to calculate daily steps
                if (stepCountOffset == 0) {
                    stepCountOffset = totalSteps
                }
                // Calculate today's steps by subtracting offset
                stepCount = totalSteps - stepCountOffset
                        updateStepsDisplay(stepCount)  // Update UI with new step count
            }
            Sensor.TYPE_STEP_DETECTOR -> {
                // Detect individual step (more responsive than step counter)
                stepCount++
                        updateStepsDisplay(stepCount)  // Update UI immediately
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("HomeFragment", "Error in sensor changed: ${e.message}", e)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Handle accuracy changes if needed
    }
    
    
    /**
     * Handles shake detection for quick mood logging
     */
    private fun handleShake() {
        try {
            // Provide haptic feedback to confirm shake detection
            vibrator?.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
            
            // Show shake detected message
            Toast.makeText(context, "Shake detected! Mood logging feature coming soon.", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            android.util.Log.e("HomeFragment", "Error handling shake: ${e.message}", e)
        }
    }
    
    /**
     * Format number with commas for better readability
     */
    private fun Int.formatWithCommas(): String {
        return String.format("%,d", this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        try {
        // Stop real-time simulation
        realtimeDataManager.stopRealtimeSimulation()
        _binding = null
            android.util.Log.d("HomeFragment", "HomeFragment destroyed")
        } catch (e: Exception) {
            android.util.Log.e("HomeFragment", "Error in onDestroyView: ${e.message}", e)
        }
    }
}