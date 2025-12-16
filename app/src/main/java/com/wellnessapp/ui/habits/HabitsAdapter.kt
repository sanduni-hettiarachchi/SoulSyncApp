package com.wellnessapp.ui.habits

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.wellnessapp.R
import com.wellnessapp.data.Habit

/**
 * Adapter for displaying habits in a RecyclerView
 */
class HabitsAdapter(
    private val onHabitClick: (Habit) -> Unit,
    private val onToggleComplete: (Habit) -> Unit,
    private val onEditHabit: (Habit) -> Unit,
    private val onDeleteHabit: (Habit) -> Unit
) : RecyclerView.Adapter<HabitsAdapter.HabitViewHolder>() {
    
    private var habitsList = listOf<Habit>()
    
    fun submitList(habits: List<Habit>) {
        habitsList = habits
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit_list, parent, false)
        return HabitViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habitsList[position]
        holder.bind(habit)
    }
    
    override fun getItemCount(): Int = habitsList.size
    
    inner class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: MaterialCardView = itemView.findViewById(R.id.card_habit)
        private val habitName: TextView = itemView.findViewById(R.id.tv_habit_name)
        private val habitIcon: android.widget.ImageView = itemView.findViewById(R.id.iv_habit_icon)
        private val editButton: android.widget.ImageView = itemView.findViewById(R.id.btn_edit_habit)
        private val deleteButton: android.widget.ImageView = itemView.findViewById(R.id.btn_delete_habit)
        private val completionCheckbox: CheckBox = itemView.findViewById(R.id.cb_habit_complete)
        private val habitCategory: TextView = itemView.findViewById(R.id.tv_habit_category)
        private val habitFrequency: TextView = itemView.findViewById(R.id.tv_habit_frequency)
        
        fun bind(habit: Habit) {
            habitName.text = habit.name
            
            // Set completion status
            completionCheckbox.isChecked = habit.isActive
            
            // Set category and frequency
            habitCategory.text = habit.category ?: "General"
            habitFrequency.text = "${habit.target} times"
            
            // Set click listeners
            cardView.setOnClickListener { onHabitClick(habit) }
            
            // Toggle completion on checkbox click
            completionCheckbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked != habit.isActive) {
                    onToggleComplete(habit)
                }
            }
            
            // Edit button click
            editButton.setOnClickListener { onEditHabit(habit) }
            
            // Delete button click
            deleteButton.setOnClickListener { onDeleteHabit(habit) }
        }
    }
}