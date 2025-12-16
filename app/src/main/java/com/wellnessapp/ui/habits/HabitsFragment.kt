package com.wellnessapp.ui.habits

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.wellnessapp.R
import com.wellnessapp.data.DataManager
import com.wellnessapp.data.Habit
import com.wellnessapp.data.RealtimeDataManager
import com.wellnessapp.databinding.FragmentHabitsBinding
// import com.wellnessapp.ui.habits.HabitDetailActivity

/**
 * Habits Fragment - Displays and manages daily habits
 * Allows users to view, add, edit, delete, and track habit completion
 */
class HabitsFragment : Fragment() {
    
    private var _binding: FragmentHabitsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var dataManager: DataManager
    private lateinit var habitsViewModel: HabitsViewModel
    private lateinit var habitsAdapter: HabitsAdapter
    private lateinit var realtimeDataManager: RealtimeDataManager
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHabitsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            android.util.Log.d("HabitsFragment", "onViewCreated started")
            
            // Initialize ViewModel first (lightweight)
            habitsViewModel = ViewModelProvider(this)[HabitsViewModel::class.java]
            android.util.Log.d("HabitsFragment", "ViewModel initialized")
            
            // Setup basic UI components first (non-blocking)
            setupBasicUI()
            showLoadingState()
            android.util.Log.d("HabitsFragment", "Basic UI setup completed")
            
            // Initialize heavy components in background thread with better lifecycle management
            // Add a small delay to ensure UI is fully rendered
            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                try {
                    // Check if fragment is still attached and visible
                    if (!isAdded || isDetached || !isVisible) {
                        android.util.Log.w("HabitsFragment", "Fragment not attached or visible, skipping background initialization")
                        return@postDelayed
                    }
                    
                    // Initialize data managers in background (heavy operations)
                    dataManager = DataManager.getInstance(requireContext())
                    android.util.Log.d("HabitsFragment", "DataManager initialized in background")
                    
                    realtimeDataManager = RealtimeDataManager.getInstance()
                    android.util.Log.d("HabitsFragment", "RealtimeDataManager initialized in background")
                    
                    // Set data manager in ViewModel
                    habitsViewModel.setDataManager(dataManager)
                    android.util.Log.d("HabitsFragment", "DataManager set in ViewModel")
                    
                    // Initialize RealtimeDataManager (heavy operation)
                    realtimeDataManager.initialize(requireContext())
                    android.util.Log.d("HabitsFragment", "RealtimeDataManager initialized in background")
                    
                    // Check if fragment is still attached before continuing
                    if (!isAdded || isDetached || !isVisible) {
                        android.util.Log.w("HabitsFragment", "Fragment detached during initialization")
                        return@postDelayed
                    }
                    
                    // Setup RecyclerView in background
                    setupRecyclerView()
                    android.util.Log.d("HabitsFragment", "RecyclerView setup completed in background")
                    
                    // Show empty state initially to prevent crashes
                    updateEmptyState(true)
                    android.util.Log.d("HabitsFragment", "Empty state set initially")
                    
                    // Setup observers after heavy initialization
                    setupObservers()
                    android.util.Log.d("HabitsFragment", "Observers setup completed")
                    
                    // Check if fragment is still attached before loading data
                    if (!isAdded || isDetached || !isVisible) {
                        android.util.Log.w("HabitsFragment", "Fragment detached before loading data")
                        return@postDelayed
                    }
                    
                    // Add sample habits if none exist
                    addSampleHabits()
                    android.util.Log.d("HabitsFragment", "Sample habits added")
                    
                    // Load habits after everything is ready
                    loadHabits()
                    android.util.Log.d("HabitsFragment", "Habits loaded")
                    
                    // Update progress display
                    updateProgressDisplay()
                    android.util.Log.d("HabitsFragment", "Progress display updated")
                    
                    // Hide loading state when ready
                    hideLoadingState()
                    android.util.Log.d("HabitsFragment", "Loading state hidden")
                    
                    android.util.Log.d("HabitsFragment", "Background initialization completed successfully")
                } catch (e: Exception) {
                    android.util.Log.e("HabitsFragment", "Error in background initialization: ${e.message}", e)
                    if (isAdded && !isDetached && isVisible) {
                        try {
                            android.widget.Toast.makeText(requireContext(), "Error loading habits: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                        } catch (toastException: Exception) {
                            android.util.Log.e("HabitsFragment", "Error showing toast: ${toastException.message}")
                        }
                    }
                }
            }, 3000) // 3 second delay to ensure UI is fully rendered and prevent ANR
            
            android.util.Log.d("HabitsFragment", "onViewCreated completed successfully")
        } catch (e: Exception) {
            android.util.Log.e("HabitsFragment", "Error in onViewCreated: ${e.message}", e)
            android.util.Log.e("HabitsFragment", "Stack trace: ${e.stackTraceToString()}")
            // Show error message to user
            android.widget.Toast.makeText(requireContext(), "Error initializing habits: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
        }
    }
    
    /**
     * Set up basic UI components (non-blocking)
     */
    private fun setupBasicUI() {
        try {
            android.util.Log.d("HabitsFragment", "Setting up basic UI components")
            
            // Set up back button
            binding.btnBack?.setOnClickListener {
                try {
                    android.util.Log.d("HabitsFragment", "Back button clicked, navigating to home")
                    findNavController().navigate(R.id.nav_home)
                } catch (e: Exception) {
                    android.util.Log.e("HabitsFragment", "Error navigating back to home: ${e.message}", e)
                }
            }
            android.util.Log.d("HabitsFragment", "Back button setup completed")
            
            // Set up empty state button
            binding.btnAddFirstHabit?.setOnClickListener {
                try {
                    showAddHabitDialog()
                } catch (e: Exception) {
                    android.util.Log.e("HabitsFragment", "Error showing add habit dialog: ${e.message}", e)
                }
            }
            
            // Set up FAB add habit button
            binding.fabAddHabit?.setOnClickListener {
                try {
                    showAddHabitDialog()
                } catch (e: Exception) {
                    android.util.Log.e("HabitsFragment", "Error showing add habit dialog from FAB: ${e.message}", e)
                }
            }
            android.util.Log.d("HabitsFragment", "Add habit buttons setup completed")
            
            android.util.Log.d("HabitsFragment", "Basic UI setup completed successfully")
        } catch (e: Exception) {
            android.util.Log.e("HabitsFragment", "Error in setupBasicUI: ${e.message}", e)
            android.util.Log.e("HabitsFragment", "Stack trace: ${e.stackTraceToString()}")
        }
    }
    
    /**
     * Set up UI components and click listeners
     */
    private fun setupUI() {
        try {
            android.util.Log.d("HabitsFragment", "Setting up UI components")
            
            // Set up back button
            binding.btnBack?.setOnClickListener {
                try {
                    android.util.Log.d("HabitsFragment", "Back button clicked, navigating to home")
                    findNavController().navigate(R.id.nav_home)
                } catch (e: Exception) {
                    android.util.Log.e("HabitsFragment", "Error navigating back to home: ${e.message}", e)
                }
            }
            android.util.Log.d("HabitsFragment", "Back button setup completed")
            
            // Floating action button removed from layout
            
            // Set up empty state button
            binding.btnAddFirstHabit?.setOnClickListener {
                try {
                    showAddHabitDialog()
                } catch (e: Exception) {
                    android.util.Log.e("HabitsFragment", "Error showing add habit dialog: ${e.message}", e)
                }
            }
            
            // Set up FAB add habit button
            binding.fabAddHabit?.setOnClickListener {
                try {
                    showAddHabitDialog()
                } catch (e: Exception) {
                    android.util.Log.e("HabitsFragment", "Error showing add habit dialog from FAB: ${e.message}", e)
                }
            }
            android.util.Log.d("HabitsFragment", "Add first habit button setup completed")
            
            // Sample habits and progress display will be handled in background thread
            android.util.Log.d("HabitsFragment", "Basic UI setup completed")
            
            android.util.Log.d("HabitsFragment", "UI setup completed successfully")
        } catch (e: Exception) {
            android.util.Log.e("HabitsFragment", "Error in setupUI: ${e.message}", e)
            android.util.Log.e("HabitsFragment", "Stack trace: ${e.stackTraceToString()}")
        }
    }
    
    /**
     * Update the progress display based on completed habits
     */
    private fun updateProgressDisplay() {
        try {
            // Check if data manager is initialized
            if (!::dataManager.isInitialized) {
                android.util.Log.w("HabitsFragment", "DataManager not initialized, skipping progress update")
                return
            }
            
            val habits = dataManager.getHabits()
            val completedHabits = habits.count { it.isActive }
            val totalHabits = habits.size
            val progressPercentage = if (totalHabits > 0) (completedHabits * 100) / totalHabits else 0
            
            // Progress display removed from layout - just log for debugging
            android.util.Log.d("HabitsFragment", "Progress updated: $completedHabits/$totalHabits ($progressPercentage%)")
        } catch (e: Exception) {
            android.util.Log.e("HabitsFragment", "Error updating progress display: ${e.message}", e)
        }
    }
    
    /**
     * Add sample habits for demonstration
     */
    private fun addSampleHabits() {
        try {
            // Check if data manager is initialized
            if (!::dataManager.isInitialized) {
                android.util.Log.w("HabitsFragment", "DataManager not initialized, skipping sample habits")
                return
            }
            
            // Check if habits already exist
            val existingHabits = dataManager.getHabits()
            if (existingHabits.isNotEmpty()) {
                android.util.Log.d("HabitsFragment", "Habits already exist, skipping sample habits")
                return // Don't add sample habits if habits already exist
            }
        
        val sampleHabits = listOf(
            Habit(
                id = "habit_1",
                name = "🌅 Wake up before 7 AM",
                description = "Start your day early for better productivity",
                type = "Morning Routine",
                target = "7:00 AM",
                createdAt = System.currentTimeMillis(),
                isActive = true
            ),
            Habit(
                id = "habit_2", 
                name = "🛏️ Make your bed",
                description = "Start your day with a small accomplishment",
                type = "Morning Routine",
                target = "5 min",
                createdAt = System.currentTimeMillis(),
                isActive = true
            ),
            Habit(
                id = "habit_3",
                name = "🧘 Meditate or stretch for 10 minutes",
                description = "Take time for mindfulness and relaxation",
                type = "Wellness",
                target = "10 min",
                createdAt = System.currentTimeMillis(),
                isActive = true
            ),
            Habit(
                id = "habit_4",
                name = "📖 Read or learn something new",
                description = "Expand your knowledge and skills daily",
                type = "Learning",
                target = "20 min",
                createdAt = System.currentTimeMillis(),
                isActive = true
            ),
            Habit(
                id = "habit_5",
                name = "🌙 Reflect on your day before sleep",
                description = "Journal or think about your day's experiences",
                type = "Evening Routine",
                target = "5 min",
                createdAt = System.currentTimeMillis(),
                isActive = true
            )
        )
        
            // Add habits to data manager
            sampleHabits.forEach { habit ->
                dataManager.saveHabit(habit)
            }
            
            android.util.Log.d("HabitsFragment", "Sample habits added successfully")
        } catch (e: Exception) {
            android.util.Log.e("HabitsFragment", "Error adding sample habits: ${e.message}", e)
            // Don't show toast here as it might be called before UI is ready
        }
    }
    
    /**
     * Show add habit dialog
     */
    private fun showAddHabitDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_habit, null)
        
        val habitNameEditText = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_habit_name)
        val habitDescriptionEditText = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_habit_description)
        val habitTypeSpinner = dialogView.findViewById<android.widget.Spinner>(R.id.spinner_habit_type)
        val habitDurationEditText = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_habit_duration)
        
        // Set up habit type spinner
        val habitTypes = arrayOf("Exercise", "Wellness", "Learning", "Productivity", "Health", "Social")
        val adapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, habitTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        habitTypeSpinner.adapter = adapter
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add New Habit")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = habitNameEditText.text.toString().trim()
                val description = habitDescriptionEditText.text.toString().trim()
                val type = habitTypeSpinner.selectedItem.toString()
                val duration = habitDurationEditText.text.toString().toIntOrNull() ?: 30
                
                if (name.isNotEmpty()) {
                    val newHabit = Habit(
                        id = "habit_${System.currentTimeMillis()}",
                        name = name,
                        description = description,
                        type = type,
                        target = "$duration min",
                        createdAt = System.currentTimeMillis(),
                        isActive = true
                    )
                    
                    dataManager.saveHabit(newHabit)
                    
                    // Notify RealtimeDataManager of new habit
                    try {
                        val allHabits = dataManager.getHabits()
                        val completedHabits = allHabits.count { it.isActive }
                        val totalHabits = allHabits.size
                        val completionPercentage = if (totalHabits > 0) (completedHabits * 100) / totalHabits else 0
                        
                        realtimeDataManager.updateHabitCompletionPercentage(completionPercentage)
                        
                        // Update the habit list in RealtimeDataManager
                        realtimeDataManager.todayHabits.value = allHabits
                        
                        android.util.Log.d("HabitsFragment", "Updated habit completion after adding: $completedHabits/$totalHabits ($completionPercentage%)")
                    } catch (e: Exception) {
                        android.util.Log.e("HabitsFragment", "Error updating realtime data after adding habit: ${e.message}", e)
                    }
                    
                    // Update progress display
                    updateProgressDisplay()
                    
                    loadHabits()
                    
                    android.widget.Toast.makeText(requireContext(), "Habit added successfully!", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    android.widget.Toast.makeText(requireContext(), "Please enter a habit name", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    /**
     * Set up RecyclerView for habits list
     */
    private fun setupRecyclerView() {
        try {
            // Create adapter with empty list initially
            habitsAdapter = HabitsAdapter(
                onHabitClick = { habit ->
                    try {
                        openHabitDetailDialog(habit)
                    } catch (e: Exception) {
                        android.util.Log.e("HabitsFragment", "Error opening habit detail: ${e.message}", e)
                        android.widget.Toast.makeText(requireContext(), "Error opening habit details", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                onToggleComplete = { habit ->
                    try {
                        toggleHabitCompletion(habit)
                    } catch (e: Exception) {
                        android.util.Log.e("HabitsFragment", "Error toggling habit completion: ${e.message}", e)
                    }
                },
                onEditHabit = { habit ->
                    try {
                        showEditHabitDialog(habit)
                    } catch (e: Exception) {
                        android.util.Log.e("HabitsFragment", "Error showing edit dialog: ${e.message}", e)
                    }
                },
                onDeleteHabit = { habit ->
                    try {
                        showDeleteConfirmation(habit)
                    } catch (e: Exception) {
                        android.util.Log.e("HabitsFragment", "Error showing delete confirmation: ${e.message}", e)
                    }
                }
            )
            
            // Set up RecyclerView with null safety
            binding.recyclerHabits?.let { recyclerView ->
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
                recyclerView.adapter = habitsAdapter
                android.util.Log.d("HabitsFragment", "RecyclerView setup completed successfully")
            } ?: run {
                android.util.Log.e("HabitsFragment", "RecyclerView is null - cannot setup")
                // Show error to user
                android.widget.Toast.makeText(requireContext(), "Error setting up habits list", android.widget.Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            android.util.Log.e("HabitsFragment", "Error setting up RecyclerView: ${e.message}", e)
            android.widget.Toast.makeText(requireContext(), "Error setting up habits list: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Set up observers for data changes
     */
    private fun setupObservers() {
        try {
            habitsViewModel.habits.observe(viewLifecycleOwner) { habits ->
                try {
                    habitsAdapter.submitList(habits)
                    updateEmptyState(habits.isEmpty())
                } catch (e: Exception) {
                    android.util.Log.e("HabitsFragment", "Error updating habits list: ${e.message}", e)
                }
            }
            
            habitsViewModel.habitsCompletionPercentage.observe(viewLifecycleOwner) { percentage ->
                try {
                    updateHeaderSummary(percentage)
                } catch (e: Exception) {
                    android.util.Log.e("HabitsFragment", "Error updating header summary: ${e.message}", e)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("HabitsFragment", "Error setting up observers: ${e.message}", e)
        }
    }
    
    /**
     * Load habits from data manager
     */
    private fun loadHabits() {
        try {
            if (::dataManager.isInitialized && ::habitsViewModel.isInitialized) {
                habitsViewModel.loadHabits(dataManager)
                android.util.Log.d("HabitsFragment", "Habits loaded successfully")
            } else {
                android.util.Log.w("HabitsFragment", "DataManager or ViewModel not initialized, skipping habit load")
                // Show empty state
                updateEmptyState(true)
            }
        } catch (e: Exception) {
            android.util.Log.e("HabitsFragment", "Error loading habits: ${e.message}", e)
            android.widget.Toast.makeText(requireContext(), "Error loading habits: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            // Show empty state on error
            updateEmptyState(true)
        }
    }
    
    /**
     * Update empty state visibility
     */
    private fun updateEmptyState(isEmpty: Boolean) {
        binding.emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.recyclerHabits.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
    
    /**
     * Update header summary with completion percentage
     */
    private fun updateHeaderSummary(percentage: Int) {
        val habits = habitsViewModel.habits.value ?: emptyList()
        val completedHabits = habits.count { habitsViewModel.isHabitCompletedForToday(it.id) }
        
        // Progress display removed from layout - just log for debugging
        android.util.Log.d("HabitsFragment", "Header summary: $completedHabits/${habits.size} habits ($percentage%)")
    }
    
    /**
     * Open habit detail dialog for editing
     */
    private fun openHabitDetailDialog(habit: Habit) {
        try {
            android.util.Log.d("HabitsFragment", "Opening habit detail dialog for habit: ${habit.name}")
            
            // Check if fragment manager is available
            if (parentFragmentManager.isStateSaved) {
                android.util.Log.e("HabitsFragment", "Fragment manager state is saved, cannot show dialog")
                android.widget.Toast.makeText(requireContext(), "Cannot open dialog at this time", android.widget.Toast.LENGTH_SHORT).show()
                return
            }
            
            val dialog = HabitDetailDialog.newInstance(
                habit = habit,
                onHabitSaved = { updatedHabit ->
                    try {
                        android.util.Log.d("HabitsFragment", "Habit saved: ${updatedHabit.name}")
                        // Update habit in data manager
                        dataManager.saveHabit(updatedHabit)
                        
                        // Update real-time data
                        updateRealtimeDataAfterHabitChange()
                        
                        // Refresh habits list
                        loadHabits()
                        
                        android.widget.Toast.makeText(requireContext(), "Habit updated successfully!", android.widget.Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        android.util.Log.e("HabitsFragment", "Error saving habit: ${e.message}", e)
                        android.widget.Toast.makeText(requireContext(), "Error saving habit: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                    }
                },
                onHabitDeleted = { deletedHabit ->
                    try {
                        android.util.Log.d("HabitsFragment", "Habit deleted: ${deletedHabit.name}")
                        // Delete habit from data manager
                        dataManager.deleteHabit(deletedHabit.id)
                        
                        // Update real-time data
                        updateRealtimeDataAfterHabitChange()
                        
                        // Refresh habits list
                        loadHabits()
                        
                        android.widget.Toast.makeText(requireContext(), "Habit deleted successfully!", android.widget.Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        android.util.Log.e("HabitsFragment", "Error deleting habit: ${e.message}", e)
                        android.widget.Toast.makeText(requireContext(), "Error deleting habit: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            )
            
            // Show dialog with error handling
            try {
                dialog.show(parentFragmentManager, "HabitDetailDialog")
                android.util.Log.d("HabitsFragment", "Habit detail dialog shown successfully")
            } catch (e: Exception) {
                android.util.Log.e("HabitsFragment", "Error showing dialog: ${e.message}", e)
                android.widget.Toast.makeText(requireContext(), "Error showing dialog: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            android.util.Log.e("HabitsFragment", "Error opening habit detail dialog: ${e.message}", e)
            android.util.Log.e("HabitsFragment", "Stack trace: ${e.stackTraceToString()}")
            android.widget.Toast.makeText(requireContext(), "Error opening habit details: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
        }
    }
    
    /**
     * Update real-time data after habit changes (add/edit/delete)
     */
    private fun updateRealtimeDataAfterHabitChange() {
        try {
            val allHabits = dataManager.getHabits()
            val completedHabits = allHabits.count { it.isActive }
            val totalHabits = allHabits.size
            val completionPercentage = if (totalHabits > 0) (completedHabits * 100) / totalHabits else 0
            
            // Update RealtimeDataManager
            realtimeDataManager.updateHabitCompletionPercentage(completionPercentage)
            realtimeDataManager.todayHabits.value = allHabits
            
            android.util.Log.d("HabitsFragment", "Updated realtime data after habit change: $completedHabits/$totalHabits ($completionPercentage%)")
        } catch (e: Exception) {
            android.util.Log.e("HabitsFragment", "Error updating realtime data after habit change: ${e.message}", e)
        }
    }
    
    /**
     * Toggle habit completion
     */
    private fun toggleHabitCompletion(habit: Habit) {
        // Toggle completion status
        val updatedHabit = habit.copy(isActive = !habit.isActive)
        dataManager.saveHabit(updatedHabit)
        
        // Notify RealtimeDataManager of habit completion change
        try {
            val allHabits = dataManager.getHabits()
            val completedHabits = allHabits.count { it.isActive }
            val totalHabits = allHabits.size
            val completionPercentage = if (totalHabits > 0) (completedHabits * 100) / totalHabits else 0
            
            // Update RealtimeDataManager with new completion data
                        realtimeDataManager.updateHabitCompletionPercentage(completionPercentage)

                        // Update the habit list in RealtimeDataManager
                        realtimeDataManager.todayHabits.value = allHabits
            
            android.util.Log.d("HabitsFragment", "Updated habit completion: $completedHabits/$totalHabits ($completionPercentage%)")
        } catch (e: Exception) {
            android.util.Log.e("HabitsFragment", "Error updating realtime data: ${e.message}", e)
        }
        
        // Show feedback
        val status = if (updatedHabit.isActive) "completed" else "incomplete"
        android.widget.Toast.makeText(requireContext(), "${habit.name} marked as $status", android.widget.Toast.LENGTH_SHORT).show()
        
        // Update progress display
        updateProgressDisplay()
        
        // Reload habits
        loadHabits()
    }
    
    /**
     * Show edit habit dialog
     */
    private fun showEditHabitDialog(habit: Habit) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_habit, null)
        
        val habitNameEditText = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_habit_name)
        val habitDescriptionEditText = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_habit_description)
        val habitTypeSpinner = dialogView.findViewById<android.widget.Spinner>(R.id.spinner_habit_type)
        val habitDurationEditText = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.et_habit_duration)
        
        // Pre-fill with existing data
        habitNameEditText.setText(habit.name)
        habitDescriptionEditText.setText(habit.description)
        habitDurationEditText.setText(habit.target.replace(" min", ""))
        
        // Set up habit type spinner
        val habitTypes = arrayOf("Exercise", "Wellness", "Learning", "Productivity", "Health", "Social")
        val adapter = android.widget.ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, habitTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        habitTypeSpinner.adapter = adapter
        
        // Set current type
        val currentTypeIndex = habitTypes.indexOf(habit.type)
        if (currentTypeIndex >= 0) {
            habitTypeSpinner.setSelection(currentTypeIndex)
        }
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Edit Habit")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name = habitNameEditText.text.toString().trim()
                val description = habitDescriptionEditText.text.toString().trim()
                val type = habitTypeSpinner.selectedItem.toString()
                val duration = habitDurationEditText.text.toString().toIntOrNull() ?: 30
                
                if (name.isNotEmpty()) {
                    val updatedHabit = habit.copy(
                        name = name,
                        description = description,
                        type = type,
                        target = "$duration min"
                    )
                    
                    dataManager.saveHabit(updatedHabit)
                    loadHabits()
                    
                    android.widget.Toast.makeText(requireContext(), "Habit updated successfully!", android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    android.widget.Toast.makeText(requireContext(), "Please enter a habit name", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    /**
     * Show delete confirmation dialog
     */
    private fun showDeleteConfirmation(habit: Habit) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Habit")
            .setMessage("Are you sure you want to delete \"${habit.name}\"? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                dataManager.deleteHabit(habit.id)
                
                // Notify RealtimeDataManager of habit deletion
                try {
                    val allHabits = dataManager.getHabits()
                    val completedHabits = allHabits.count { it.isActive }
                    val totalHabits = allHabits.size
                    val completionPercentage = if (totalHabits > 0) (completedHabits * 100) / totalHabits else 0
                    
                        realtimeDataManager.updateHabitCompletionPercentage(completionPercentage)

                        // Update the habit list in RealtimeDataManager
                        realtimeDataManager.todayHabits.value = allHabits
                    
                    android.util.Log.d("HabitsFragment", "Updated habit completion after deletion: $completedHabits/$totalHabits ($completionPercentage%)")
                } catch (e: Exception) {
                    android.util.Log.e("HabitsFragment", "Error updating realtime data after deletion: ${e.message}", e)
                }
                
                // Update progress display
                updateProgressDisplay()
                
                loadHabits()
                android.widget.Toast.makeText(requireContext(), "Habit deleted successfully!", android.widget.Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    /**
     * Show loading state to prevent ANR
     */
    private fun showLoadingState() {
        try {
            // Show loading indicators immediately with more aggressive dimming
            binding.root.alpha = 0.5f
            android.util.Log.d("HabitsFragment", "Loading state shown")
        } catch (e: Exception) {
            android.util.Log.e("HabitsFragment", "Error showing loading state: ${e.message}", e)
        }
    }
    
    /**
     * Hide loading state when data is ready
     */
    private fun hideLoadingState() {
        try {
            // Hide loading indicators and restore full opacity
            binding.root.alpha = 1.0f
            android.util.Log.d("HabitsFragment", "Loading state hidden")
        } catch (e: Exception) {
            android.util.Log.e("HabitsFragment", "Error hiding loading state: ${e.message}", e)
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
