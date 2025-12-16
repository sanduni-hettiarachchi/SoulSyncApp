package com.wellnessapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.wellnessapp.databinding.ActivitySigninBinding

class SignInActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivitySigninBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        android.util.Log.d("SignInActivity", "SignInActivity created")
        
        try {
            binding = ActivitySigninBinding.inflate(layoutInflater)
            setContentView(binding.root)
            android.util.Log.d("SignInActivity", "Layout set successfully")
            
            setupClickListeners()
            android.util.Log.d("SignInActivity", "Click listeners setup completed")
        } catch (e: Exception) {
            android.util.Log.e("SignInActivity", "Error in onCreate: ${e.message}", e)
        }
    }
    
    private fun setupClickListeners() {
        binding.btnSignin.setOnClickListener {
            // Simple sign in - no validation needed
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            
            // Store user credentials (for demo purposes)
            val sharedPrefs = getSharedPreferences("wellness_app", Context.MODE_PRIVATE)
            sharedPrefs.edit()
                .putString("user_email", if (email.isNotEmpty()) email else "user@example.com")
                .putString("user_name", if (email.isNotEmpty()) email.substringBefore("@") else "User")
                .putBoolean("user_signed_in", true)
                .apply()
            
            Toast.makeText(this, "Sign in successful! Welcome back!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        
        binding.tvSignupLink.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }
}
