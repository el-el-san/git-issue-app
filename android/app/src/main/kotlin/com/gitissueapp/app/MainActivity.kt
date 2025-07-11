package com.gitissueapp.app

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("GitIssueApp", "MainActivity onCreate started")
        super.onCreate(savedInstanceState)
        
        try {
            Log.d("GitIssueApp", "Setting content view")
            setContentView(R.layout.activity_main)
            
            // Try to update the status text
            try {
                val statusText = findViewById<TextView>(R.id.status_text)
                statusText?.text = "App started successfully! 🎉"
                Log.d("GitIssueApp", "Status text updated")
            } catch (e: Exception) {
                Log.e("GitIssueApp", "Failed to update status text", e)
            }
            
            Log.d("GitIssueApp", "MainActivity onCreate completed successfully")
            
        } catch (e: Exception) {
            Log.e("GitIssueApp", "Critical error in MainActivity", e)
            // Create a simple fallback view
            val textView = TextView(this)
            textView.text = "Basic App Started - Error: ${e.message}"
            textView.textSize = 16f
            textView.setPadding(32, 32, 32, 32)
            setContentView(textView)
        }
    }
}