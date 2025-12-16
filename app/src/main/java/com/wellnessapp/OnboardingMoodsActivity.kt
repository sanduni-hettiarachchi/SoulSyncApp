package com.wellnessapp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class OnboardingMoodsActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Hide action bar for full screen experience
        supportActionBar?.hide()
        
        setContentView(R.layout.activity_onboarding_moods)
        
        setupUI()
    }
    
    private fun setupUI() {
        findViewById<com.google.android.material.button.MaterialButton>(R.id.btnContinue).setOnClickListener {
            // Navigate to next onboarding screen (tools)
            val intent = Intent(this, OnboardingToolsActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
