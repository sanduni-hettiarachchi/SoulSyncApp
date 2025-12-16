package com.wellnessapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class OnboardingDrinkActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        android.util.Log.d("OnboardingDrink", "OnboardingDrinkActivity created")
        
        try {
            // Hide action bar for full screen experience
            supportActionBar?.hide()
            
            setContentView(R.layout.activity_onboarding_drink)
            android.util.Log.d("OnboardingDrink", "Layout set successfully")
            
            // Show a toast to confirm the activity is running
            android.widget.Toast.makeText(this, "Onboarding Started!", android.widget.Toast.LENGTH_SHORT).show()
            
            setupUI()
            android.util.Log.d("OnboardingDrink", "UI setup completed")
        } catch (e: Exception) {
            android.util.Log.e("OnboardingDrink", "Error in onCreate: ${e.message}", e)
        }
    }
    
    private fun setupUI() {
        try {
            android.util.Log.d("OnboardingDrink", "Setting up UI")
            val continueButton = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnContinue)
            android.util.Log.d("OnboardingDrink", "Continue button found: $continueButton")
            
            continueButton.setOnClickListener {
                android.util.Log.d("OnboardingDrink", "Continue button clicked")
                // Navigate to next onboarding screen (moods)
                val intent = Intent(this, OnboardingMoodsActivity::class.java)
                startActivity(intent)
                finish()
            }
            android.util.Log.d("OnboardingDrink", "UI setup completed successfully")
        } catch (e: Exception) {
            android.util.Log.e("OnboardingDrink", "Error in setupUI: ${e.message}", e)
        }
    }
}
