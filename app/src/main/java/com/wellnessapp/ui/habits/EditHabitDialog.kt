package com.wellnessapp.ui.habits

import android.app.Dialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.wellnessapp.R
import com.wellnessapp.data.Habit

/**
 * Dialog for editing an existing habit
 */
class EditHabitDialog : DialogFragment() {

    private var habit: Habit? = null
    private var onHabitSaved: ((Habit) -> Unit)? = null

    companion object {
        private const val ARG_HABIT = "habit"

        fun newInstance(habit: Habit, onHabitSaved: (Habit) -> Unit): EditHabitDialog {
            val args = Bundle().apply {
                putSerializable(ARG_HABIT, habit)
            }
            return EditHabitDialog().apply {
                arguments = args
                this.onHabitSaved = onHabitSaved
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_edit_habit)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        // Get habit from arguments
        habit = arguments?.getSerializable(ARG_HABIT) as? Habit

        setupUI(dialog)
        setupListeners(dialog)

        return dialog
    }

    private fun setupUI(dialog: Dialog) {
        val etHabitName = dialog.findViewById<TextInputEditText>(R.id.et_habit_name)
        val etHabitDescription = dialog.findViewById<TextInputEditText>(R.id.et_habit_description)
        val spinnerCategory = dialog.findViewById<Spinner>(R.id.spinner_category)

        // Populate fields with current habit data
        habit?.let { currentHabit ->
            etHabitName.setText(currentHabit.name)
            etHabitDescription.setText(currentHabit.description)
        }

        // Setup category spinner
        val categories = listOf("Health", "Fitness", "Mental", "Work", "Personal", "General")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCategory.adapter = adapter

        // Set current category
        habit?.category?.let { currentCategory ->
            val categoryIndex = categories.indexOf(currentCategory)
            if (categoryIndex >= 0) {
                spinnerCategory.setSelection(categoryIndex)
            }
        }
    }

    private fun setupListeners(dialog: Dialog) {
        val btnCancel = dialog.findViewById<MaterialButton>(R.id.btn_cancel)
        val btnSave = dialog.findViewById<MaterialButton>(R.id.btn_save)

        btnCancel.setOnClickListener {
            dismiss()
        }

        btnSave.setOnClickListener {
            saveHabit(dialog)
        }
    }

    private fun saveHabit(dialog: Dialog) {
        val etHabitName = dialog.findViewById<TextInputEditText>(R.id.et_habit_name)
        val etHabitDescription = dialog.findViewById<TextInputEditText>(R.id.et_habit_description)
        val spinnerCategory = dialog.findViewById<Spinner>(R.id.spinner_category)

        val name = etHabitName.text.toString().trim()
        val description = etHabitDescription.text.toString().trim()
        val category = spinnerCategory.selectedItem.toString()

        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a habit name", Toast.LENGTH_SHORT).show()
            return
        }

        // Create updated habit
        val updatedHabit = habit?.copy(
            name = name,
            description = description,
            category = category
        ) ?: return

        onHabitSaved?.invoke(updatedHabit)
        dismiss()
    }
}
