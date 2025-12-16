package com.wellnessapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.wellnessapp.databinding.ActivityMoodSelectionBinding
import com.wellnessapp.data.Mood
import com.wellnessapp.data.DataManager
import java.util.UUID

class MoodSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMoodSelectionBinding
    private lateinit var dataManager: DataManager
    private var selectedMood: String = ""
    private var selectedEmoji: String = "😐"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMoodSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dataManager = DataManager.getInstance(this)

        // Get emoji from intent
        selectedEmoji = intent.getStringExtra("emoji") ?: "😐"
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

        // Mood option buttons
        val moodButtons = listOf(
            binding.btnAnxious to "Anxious",
            binding.btnBored to "Bored",
            binding.btnBusy to "Busy",
            binding.btnCalm to "Calm",
            binding.btnConfused to "Confused",
            binding.btnFine to "Fine",
            binding.btnFrustrated to "Frustrated",
            binding.btnDistant to "Distant",
            binding.btnDistracted to "Distracted",
            binding.btnStressed to "Stressed",
            binding.btnTired to "Tired"
        )

        moodButtons.forEach { (button, mood) ->
            button.setOnClickListener {
                selectMood(mood, button)
            }
        }

        // Action button
        binding.btnHereHowFeel.setOnClickListener {
            if (selectedMood.isNotEmpty()) {
                proceedToFactors()
            } else {
                Toast.makeText(this, "Please select a mood", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun selectMood(mood: String, button: com.google.android.material.button.MaterialButton) {
        // Reset all buttons
        resetAllMoodButtons()
        
        // Select current button
        selectedMood = mood
        button.setBackgroundColor(resources.getColor(R.color.primary_teal, null))
        button.setTextColor(resources.getColor(R.color.text_white, null))
    }

    private fun resetAllMoodButtons() {
        val moodButtons = listOf(
            binding.btnAnxious,
            binding.btnBored,
            binding.btnBusy,
            binding.btnCalm,
            binding.btnConfused,
            binding.btnFine,
            binding.btnFrustrated,
            binding.btnDistant,
            binding.btnDistracted,
            binding.btnStressed,
            binding.btnTired
        )

        moodButtons.forEach { button ->
            button.setBackgroundColor(resources.getColor(R.color.background_light_gray, null))
            button.setTextColor(resources.getColor(R.color.text_secondary, null))
        }
    }

    private fun proceedToFactors() {
        val intent = Intent(this, MoodFactorsSelectionActivity::class.java).apply {
            putExtra("emoji", selectedEmoji)
            putExtra("mood", selectedMood)
        }
        startActivity(intent)
        finish()
    }
}
