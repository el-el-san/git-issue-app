package com.gitissueapp.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.gitissueapp.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Set welcome message
        binding.welcomeText.text = "🎉 GitHub Issue Manager\n\nPhase 2: Basic UI Structure\nReady to add features!"
    }
}