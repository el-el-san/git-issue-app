package com.gitissueapp.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            // Set edge-to-edge
            WindowCompat.setDecorFitsSystemWindows(window, false)
            
            // Set the layout
            setContentView(R.layout.activity_main)
            
            // For now, just show a simple welcome message
            // Hilt DI and other features will be implemented later
            
        } catch (e: Exception) {
            e.printStackTrace()
            // If something fails, still try to show basic layout
            setContentView(android.R.layout.activity_list_item)
        }
    }
}