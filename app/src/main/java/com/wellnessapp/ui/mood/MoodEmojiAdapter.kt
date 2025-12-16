package com.wellnessapp.ui.mood

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.wellnessapp.R
import com.wellnessapp.data.Mood

/**
 * Adapter for displaying mood emojis in a horizontal list
 * Allows users to select their current mood
 */
class MoodEmojiAdapter(
    private val moods: List<Mood>,
    private val onMoodSelected: (Mood) -> Unit
) : RecyclerView.Adapter<MoodEmojiAdapter.MoodViewHolder>() {
    
    private var selectedPosition = -1
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mood_emoji, parent, false)
        return MoodViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        holder.bind(moods[position], position == selectedPosition)
    }
    
    override fun getItemCount(): Int = moods.size
    
    inner class MoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: MaterialCardView = itemView.findViewById(R.id.card_mood_emoji)
        private val textEmoji: TextView = itemView.findViewById(R.id.tv_mood_emoji)
        private val textDescription: TextView = itemView.findViewById(R.id.tv_mood_description)
        
        fun bind(mood: Mood, isSelected: Boolean) {
            textEmoji.text = mood.emoji
            textDescription.text = mood.getMoodDescription()
            
            // Update selection state
            if (isSelected) {
                cardView.setCardBackgroundColor(itemView.context.getColor(R.color.primary_green))
                textEmoji.setTextColor(itemView.context.getColor(R.color.text_white))
                textDescription.setTextColor(itemView.context.getColor(R.color.text_white))
            } else {
                // Set card color based on mood type
                val colorRes = itemView.context.resources.getIdentifier(mood.color, "color", itemView.context.packageName)
                if (colorRes != 0) {
                    cardView.setCardBackgroundColor(itemView.context.getColor(colorRes))
                } else {
                    cardView.setCardBackgroundColor(itemView.context.getColor(R.color.background_white))
                }
                
                textEmoji.setTextColor(itemView.context.getColor(R.color.text_primary))
                textDescription.setTextColor(itemView.context.getColor(R.color.text_primary))
            }
            
            // Set click listener
            itemView.setOnClickListener {
                val previousPosition = selectedPosition
                selectedPosition = adapterPosition
                
                // Notify changes
                if (previousPosition != -1) {
                    notifyItemChanged(previousPosition)
                }
                notifyItemChanged(selectedPosition)
                
                // Call callback
                onMoodSelected(mood)
            }
        }
    }
}
