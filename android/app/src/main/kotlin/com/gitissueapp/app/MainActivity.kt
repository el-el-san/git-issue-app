package com.gitissueapp.app

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import android.graphics.Color

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Create the simplest possible UI
        val textView = TextView(this).apply {
            text = "✅ GitIssue App\n\nKotlin Implementation\nWorking Successfully!"
            textSize = 18f
            setPadding(64, 64, 64, 64)
            setTextColor(Color.BLACK)
            setBackgroundColor(Color.WHITE)
        }
        
        setContentView(textView)
    }
}