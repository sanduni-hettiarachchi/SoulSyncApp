package com.wellnessapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class OnboardingToolsActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Hide action bar for full screen experience
        supportActionBar?.hide()
        
        setContentView(R.layout.activity_onboarding_tools)
        
        setupUI()
    }
    
    private fun setupUI() {
        try {
            android.util.Log.d("OnboardingTools", "Setting up UI")
            val button = findViewById<com.google.android.material.button.MaterialButton>(R.id.btnGetStarted)
            android.util.Log.d("OnboardingTools", "Button found: $button")
            
            button.setOnClickListener {
                try {
                    android.util.Log.d("OnboardingTools", "Get Started button clicked")
                    
                    // Mark onboarding as completed
                    val sharedPrefs = getSharedPreferences("wellness_app", MODE_PRIVATE)
                    sharedPrefs.edit().putBoolean("onboarding_completed", true).apply()
                    android.util.Log.d("OnboardingTools", "Onboarding marked as completed")
                    
                    // Navigate to sign-in page (users can choose sign-in or sign-up)
                    val intent = Intent(this, SignInActivity::class.java)
                    android.util.Log.d("OnboardingTools", "Starting SignInActivity")
                    startActivity(intent)
                    finish()
                    android.util.Log.d("OnboardingTools", "OnboardingToolsActivity finished")
                } catch (e: Exception) {
                    android.util.Log.e("OnboardingTools", "Error in button click: ${e.message}", e)
                }
            }
            android.util.Log.d("OnboardingTools", "Button click listener set successfully")
        } catch (e: Exception) {
            android.util.Log.e("OnboardingTools", "Error setting up UI: ${e.message}", e)
        }
    }
}
