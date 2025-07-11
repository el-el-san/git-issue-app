package com.gitissueapp.app

import android.app.Application
import android.util.Log

class GitIssueApplication : Application() {
    
    override fun onCreate() {
        Log.d("GitIssueApp", "Application onCreate started")
        super.onCreate()
        Log.d("GitIssueApp", "Application onCreate completed")
    }
}