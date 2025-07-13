package com.gitissueapp.app.data.storage

import android.content.Context
import android.content.SharedPreferences

class AppPreferences(context: Context) {
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        "app_preferences", 
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val KEY_REPOSITORY_OWNER = "repository_owner"
        private const val KEY_REPOSITORY_NAME = "repository_name"
        private const val KEY_LAST_REPOSITORY = "last_repository"
        private const val KEY_IS_FIRST_LAUNCH = "is_first_launch"
    }
    
    // Repository settings
    fun saveRepository(owner: String, name: String) {
        sharedPreferences.edit()
            .putString(KEY_REPOSITORY_OWNER, owner)
            .putString(KEY_REPOSITORY_NAME, name)
            .putString(KEY_LAST_REPOSITORY, "$owner/$name")
            .apply()
    }
    
    fun getRepositoryOwner(): String? {
        return sharedPreferences.getString(KEY_REPOSITORY_OWNER, null)
    }
    
    fun getRepositoryName(): String? {
        return sharedPreferences.getString(KEY_REPOSITORY_NAME, null)
    }
    
    fun getLastRepository(): String? {
        return sharedPreferences.getString(KEY_LAST_REPOSITORY, null)
    }
    
    fun hasRepositorySet(): Boolean {
        return getRepositoryOwner() != null && getRepositoryName() != null
    }
    
    // App state
    fun setFirstLaunch(isFirstLaunch: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_IS_FIRST_LAUNCH, isFirstLaunch)
            .apply()
    }
    
    fun isFirstLaunch(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_FIRST_LAUNCH, true)
    }
    
    // Clear all preferences
    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }
}