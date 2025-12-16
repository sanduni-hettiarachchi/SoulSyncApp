package com.wellnessapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.wellnessapp.databinding.ActivitySignupBinding

class SignUpActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySignupBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        android.util.Log.d("SignUpActivity", "SignUpActivity created")
        
        try {
            binding = ActivitySignupBinding.inflate(layoutInflater)
            setContentView(binding.root)
            android.util.Log.d("SignUpActivity", "Layout set successfully")
            
            setupClickListeners()
            android.util.Log.d("SignUpActivity", "Click listeners setup completed")
        } catch (e: Exception) {
            android.util.Log.e("SignUpActivity", "Error in onCreate: ${e.message}", e)
        }
    }
    
    private fun setupClickListeners() {
        binding.btnBack.setOnClickListener {
            finish()
        }
        
        binding.btnSignup.setOnClickListener {
            // Simple sign up - minimal validation
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val termsAccepted = binding.cbTerms.isChecked
            
            if (!termsAccepted) {
                Toast.makeText(this, "Please accept the terms and conditions", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // Store user credentials (for demo purposes)
            val sharedPrefs = getSharedPreferences("wellness_app", MODE_PRIVATE)
            sharedPrefs.edit()
                .putString("user_email", if (email.isNotEmpty()) email else "user@example.com")
                .putString("user_name", if (email.isNotEmpty()) email.substringBefore("@") else "User")
                .putBoolean("user_signed_in", true)
                .apply()
            
            Toast.makeText(this, "Sign up successful! Welcome to SoulSync!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        
        binding.tvSigninLink.setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }
        
        binding.cbTerms.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(this, "Terms & Conditions accepted", Toast.LENGTH_SHORT).show()
            }
        }
        
        binding.tvTermsText.setOnClickListener {
            // Toggle the checkbox when terms text is clicked
            binding.cbTerms.isChecked = !binding.cbTerms.isChecked
            Toast.makeText(this, "Terms & Conditions", Toast.LENGTH_SHORT).show()
        }
    }
}
