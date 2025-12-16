package com.wellnessapp.ui.habits

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.wellnessapp.R

/**
 * Adapter for displaying habit icons in a grid
 * Allows users to select an icon for their habit
 */
class HabitIconAdapter(
    private val icons: List<String>,
    private val onIconSelected: (String) -> Unit
) : RecyclerView.Adapter<HabitIconAdapter.IconViewHolder>() {
    
    private var selectedPosition = 0
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit_icon, parent, false)
        return IconViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
        holder.bind(icons[position], position == selectedPosition)
    }
    
    override fun getItemCount(): Int = icons.size
    
    inner class IconViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: MaterialCardView = itemView.findViewById(R.id.card_icon)
        private val textIcon: TextView = itemView.findViewById(R.id.tv_icon)
        
        fun bind(icon: String, isSelected: Boolean) {
            textIcon.text = icon
            
            // Update selection state
            if (isSelected) {
                cardView.setCardBackgroundColor(itemView.context.getColor(R.color.primary_green))
                textIcon.setTextColor(itemView.context.getColor(R.color.text_white))
            } else {
                cardView.setCardBackgroundColor(itemView.context.getColor(R.color.background_white))
                textIcon.setTextColor(itemView.context.getColor(R.color.text_primary))
            }
            
            // Set click listener
            itemView.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = adapterPosition
                
                // Notify changes
                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)
                
                // Call callback
                onIconSelected(icon)
            }
        }
    }
}
