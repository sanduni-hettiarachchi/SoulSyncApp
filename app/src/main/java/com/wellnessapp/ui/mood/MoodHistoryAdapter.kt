package com.wellnessapp.ui.mood

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wellnessapp.R
import com.wellnessapp.data.Mood
import java.text.SimpleDateFormat
import java.util.*

class MoodHistoryAdapter(
    private val onMoodClick: (Mood) -> Unit
) : RecyclerView.Adapter<MoodHistoryAdapter.MoodViewHolder>() {
    
    private var moods: List<Mood> = emptyList()
    
    fun updateMoods(newMoods: List<Mood>) {
        moods = newMoods.sortedByDescending { it.timestamp }
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mood_history, parent, false)
        return MoodViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        holder.bind(moods[position])
    }
    
    override fun getItemCount(): Int = moods.size
    
    inner class MoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvMoodEmoji: TextView = itemView.findViewById(R.id.tv_mood_emoji)
        private val tvMoodTime: TextView = itemView.findViewById(R.id.tv_mood_time)
        private val tvMoodDate: TextView = itemView.findViewById(R.id.tv_mood_date)
        
        fun bind(mood: Mood) {
            tvMoodEmoji.text = mood.emoji
            
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            
            tvMoodTime.text = timeFormat.format(Date(mood.timestamp))
            tvMoodDate.text = dateFormat.format(Date(mood.timestamp))
            
            itemView.setOnClickListener {
                onMoodClick(mood)
            }
        }
    }
}
