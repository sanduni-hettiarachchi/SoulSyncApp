package com.wellnessapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.wellnessapp.databinding.ActivityMoodFactorsSelectionBinding
import com.wellnessapp.data.Mood
import com.wellnessapp.data.DataManager
import java.util.UUID

class MoodFactorsSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMoodFactorsSelectionBinding
    private lateinit var dataManager: DataManager
    private var selectedFactors = mutableSetOf<String>()
    private var selectedEmoji: String = ""
    private var selectedMood: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoodFactorsSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dataManager = DataManager.getInstance(this)

        // Get data from intent
        selectedEmoji = intent.getStringExtra("emoji") ?: "😐"
        selectedMood = intent.getStringExtra("mood") ?: ""

        binding.tvLargeEmoji.text = selectedEmoji

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Back button
        binding.ivBack.setOnClickListener {
            finish()
        }

        // Close button
        binding.ivClose.setOnClickListener {
            finish()
        }

        // Factor cards
        val factorCards = listOf(
            binding.cardRelationship to "Relationship",
            binding.cardWeather to "Weather",
            binding.cardWork to "Work",
            binding.cardSchool to "School",
            binding.cardExercise to "Exercise",
            binding.cardHealth to "Health",
            binding.cardFamily to "Family",
            binding.cardHobbies to "Hobbies",
            binding.cardSleep to "Sleep",
            binding.cardFriends to "Friends",
            binding.cardFinances to "Finances",
            binding.cardLocation to "Location"
        )

        factorCards.forEach { (card, factor) ->
            card.setOnClickListener {
                toggleFactor(factor, card)
            }
        }

        // Action button
        binding.btnBecauseOfThis.setOnClickListener {
            saveMoodEntry()
        }
    }

    private fun toggleFactor(factor: String, card: com.google.android.material.card.MaterialCardView) {
        if (selectedFactors.contains(factor)) {
            // Deselect
            selectedFactors.remove(factor)
            card.setCardBackgroundColor(resources.getColor(R.color.background_white, null))
            // Reset text and icon colors
            updateCardColors(card, false)
        } else {
            // Select
            selectedFactors.add(factor)
            card.setCardBackgroundColor(resources.getColor(R.color.primary_indigo, null))
            // Set text and icon colors to white
            updateCardColors(card, true)
        }
    }

    private fun updateCardColors(card: com.google.android.material.card.MaterialCardView, isSelected: Boolean) {
        val textColor = if (isSelected) R.color.text_white else R.color.text_primary
        val iconTint = if (isSelected) R.color.text_white else R.color.text_primary
        
        // This is a simplified approach - in a real app you'd need to find the TextViews and ImageViews
        // and update their colors programmatically
    }

    private fun saveMoodEntry() {
        val note = binding.etMoodNote.text.toString().trim()
        
        val mood = Mood(
            id = UUID.randomUUID().toString(),
            emoji = selectedEmoji,
            notes = note,
            date = System.currentTimeMillis(),
            color = when (selectedMood) {
                "Happy" -> "mood_happy"
                "Sad" -> "mood_sad"
                "Busy" -> "mood_busy"
                else -> "mood_neutral"
            }
        )

        // Save mood to data manager
        dataManager.addMood(mood)

        // Show success message
        Toast.makeText(this, "Mood logged successfully! ${selectedEmoji}", Toast.LENGTH_SHORT).show()

        // Finish and return to home
        finish()
    }
}
