package com.wellnessapp

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TestHomeActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Hide action bar
        supportActionBar?.hide()
        
        // Create a simple layout programmatically
        val textView = TextView(this)
        textView.text = "Welcome to SoulSync!\n\nThis is the home page.\n\nApp is working correctly!"
        textView.textSize = 18f
        textView.setPadding(50, 50, 50, 50)
        textView.setBackgroundColor(android.graphics.Color.WHITE)
        
        setContentView(textView)
    }
}
