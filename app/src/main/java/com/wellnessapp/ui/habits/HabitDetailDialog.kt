package com.wellnessapp.ui.habits

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.wellnessapp.R
import com.wellnessapp.data.Habit
import android.widget.EditText
import android.widget.Spinner
import android.widget.Button
import android.widget.TextView

/**
 * Dialog for adding/editing habits
 * Provides a comprehensive form for habit creation and modification
 */
class HabitDetailDialog : DialogFragment() {

    private lateinit var etHabitName: EditText
    private lateinit var etHabitDescription: EditText
    private lateinit var spFrequency: Spinner
    private lateinit var etDuration: EditText
    private lateinit var btnSave: Button
    private lateinit var btnCancel: Button
    private lateinit var btnDelete: Button

    private var habit: Habit? = null
    private var onHabitSaved: ((Habit) -> Unit)? = null
    private var onHabitDeleted: ((Habit) -> Unit)? = null

    companion object {
        private const val ARG_HABIT = "habit"

        fun newInstance(habit: Habit? = null, onHabitSaved: (Habit) -> Unit, onHabitDeleted: ((Habit) -> Unit)? = null): HabitDetailDialog {
            val args = Bundle().apply {
                habit?.let { putSerializable(ARG_HABIT, it) }
            }
            return HabitDetailDialog().apply {
                arguments = args
                this.onHabitSaved = onHabitSaved
                this.onHabitDeleted = onHabitDeleted
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            habit = it.getSerializable(ARG_HABIT) as? Habit
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.dialog_add_habit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupListeners()
    }

    /**
     * Sets up the UI components and populates fields if editing
     */
    private fun setupUI() {
        try {
            // Initialize views with null safety
            val rootView = view ?: return
            etHabitName = rootView.findViewById(R.id.et_habit_name)
            etHabitDescription = rootView.findViewById(R.id.et_habit_description)
            spFrequency = rootView.findViewById(R.id.spinner_habit_type)
            etDuration = rootView.findViewById(R.id.et_habit_duration)
            btnSave = rootView.findViewById(R.id.btn_save_habit)
            btnCancel = rootView.findViewById(R.id.btn_cancel)
            btnDelete = rootView.findViewById(R.id.btn_delete_habit)
            
            // Check if all required views are found
            if (etHabitName == null || etHabitDescription == null || spFrequency == null || 
                etDuration == null || btnSave == null || btnCancel == null || btnDelete == null) {
                android.util.Log.e("HabitDetailDialog", "Some required views are null")
                return
            }
        
            // Set up habit type spinner
            val habitTypes = arrayOf("Water", "Exercise", "Meditation", "Reading", "Sleep", "Other")
            val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, habitTypes)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spFrequency.adapter = adapter

            // Show delete button only when editing existing habit
            if (habit != null) {
                btnDelete.visibility = View.VISIBLE
            }

            // Populate fields if editing existing habit
            habit?.let { existingHabit ->
                etHabitName.setText(existingHabit.name)
                etHabitDescription.setText(existingHabit.description)
                etDuration.setText(existingHabit.duration.toString())
            
                // Set spinner selection
                val typeIndex = habitTypes.indexOf(existingHabit.type.capitalize())
                if (typeIndex >= 0) {
                    spFrequency.setSelection(typeIndex)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("HabitDetailDialog", "Error in setupUI: ${e.message}", e)
        }
    }

    /**
     * Sets up click listeners for dialog buttons
     */
    private fun setupListeners() {
        try {
            btnSave?.setOnClickListener {
                saveHabit()
            }
            
            btnCancel?.setOnClickListener {
                dismiss()
            }
            
            btnDelete?.setOnClickListener {
                deleteHabit()
            }
        } catch (e: Exception) {
            android.util.Log.e("HabitDetailDialog", "Error in setupListeners: ${e.message}", e)
        }
    }

    /**
     * Validates form data and saves the habit
     */
    private fun saveHabit() {
        val name = etHabitName.text.toString().trim()
        val description = etHabitDescription.text.toString().trim()
        val duration = etDuration.text.toString().trim().toIntOrNull() ?: 30
        val type = spFrequency.selectedItem.toString().lowercase()

        // Validate required fields
        if (name.isEmpty()) {
            etHabitName.error = "Habit name is required"
            return
        }

        // Create or update habit
        val habitToSave = habit?.copy(
            name = name,
            description = description,
            duration = duration,
            type = type
        ) ?: Habit(
            id = System.currentTimeMillis().toString(),
            name = name,
            description = description,
            target = "$duration minutes", // Use duration as target
            duration = duration,
            type = type,
            icon = getIconForType(type),
            color = getColorForType(type)
        )

        onHabitSaved?.invoke(habitToSave)
        dismiss()
    }
    
    /**
     * Deletes the current habit
     */
    private fun deleteHabit() {
        habit?.let { habitToDelete ->
            onHabitDeleted?.invoke(habitToDelete)
            dismiss()
        }
    }

    /**
     * Returns appropriate icon for habit type
     */
    private fun getIconForType(type: String): String {
        return when (type.lowercase()) {
            "water" -> "ic_habit_water"
            "exercise" -> "ic_habit_exercise"
            "meditation" -> "ic_habit_meditation"
            "reading" -> "ic_habit_reading"
            "sleep" -> "ic_habit_sleep"
            else -> "ic_habit_default"
        }
    }

    /**
     * Returns appropriate color for habit type
     */
    private fun getColorForType(type: String): String {
        return when (type.lowercase()) {
            "water" -> "primary_light_green"
            "exercise" -> "accent_orange"
            "meditation" -> "secondary_light_blue"
            "reading" -> "primary_teal"
            "sleep" -> "primary_purple"
            else -> "primary_gray"
        }
    }

}
