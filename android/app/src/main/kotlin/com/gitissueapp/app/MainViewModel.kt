package com.gitissueapp.app

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gitissueapp.app.data.api.GitHubClient
import com.gitissueapp.app.data.model.Issue
import com.gitissueapp.app.data.storage.AuthTokenStorage
import com.gitissueapp.app.data.storage.AppPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MainUiState(
    val isLoading: Boolean = false,
    val issues: List<Issue> = emptyList(),
    val error: String? = null,
    val debugLog: String = "",
    val isAuthenticated: Boolean = false,
    val currentRepository: String = ""
)

class MainViewModel(context: Context) : ViewModel() {
    
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.asStateFlow()
    
    private val authTokenStorage = AuthTokenStorage(context)
    private val appPreferences = AppPreferences(context)
    private val gitHubClient = GitHubClient(authTokenStorage)
    
    // Repository settings (restored from preferences)
    private var currentOwner: String
    private var currentRepo: String
    
    private var debugLogBuilder = StringBuilder()
    
    init {
        // Restore repository settings or use defaults
        val savedOwner = appPreferences.getRepositoryOwner()
        val savedRepo = appPreferences.getRepositoryName()
        
        if (savedOwner != null && savedRepo != null) {
            currentOwner = savedOwner
            currentRepo = savedRepo
            addLog("Restored repository: $currentOwner/$currentRepo")
        } else {
            // Default repository - this repository
            currentOwner = "el-el-san"
            currentRepo = "git-issue-app"
            addLog("Using default repository: $currentOwner/$currentRepo")
        }
        
        // Check authentication status
        val isAuthenticated = authTokenStorage.isAuthenticated()
        addLog("Authentication status: ${if (isAuthenticated) "Authenticated" else "Not authenticated"}")
        
        if (isAuthenticated) {
            val token = authTokenStorage.getAccessToken()
            addLog("Token exists: ${token?.take(10)}...")
        }
        
        // Update UI state
        _uiState.value = _uiState.value.copy(
            isAuthenticated = isAuthenticated,
            currentRepository = "$currentOwner/$currentRepo"
        )
        
        // Mark as not first launch after initialization
        if (appPreferences.isFirstLaunch()) {
            appPreferences.setFirstLaunch(false)
            addLog("First app launch completed")
        }
    }
    
    private fun addLog(message: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        debugLogBuilder.append("[$timestamp] $message\n")
        _uiState.value = _uiState.value.copy(debugLog = debugLogBuilder.toString())
        android.util.Log.d("GitHubAPI", message)
    }
    
    fun loadIssues() {
        viewModelScope.launch {
            val currentState = _uiState.value
            _uiState.value = currentState.copy(isLoading = true, error = null)
            
            try {
                addLog("Starting to load issues from $currentOwner/$currentRepo")
                addLog("URL: https://api.github.com/repos/$currentOwner/$currentRepo/issues")
                addLog("Sending HTTP request to GitHub API...")
                val issues = gitHubClient.getIssues(currentOwner, currentRepo)
                addLog("✅ Successfully loaded ${issues.size} issues")
                if (issues.isNotEmpty()) {
                    addLog("First issue: #${issues[0].number} ${issues[0].title}")
                    addLog("Author: ${issues[0].user.login}")
                }
                _uiState.value = currentState.copy(
                    isLoading = false, 
                    issues = issues,
                    debugLog = debugLogBuilder.toString()
                )
            } catch (e: Exception) {
                addLog("❌ Error loading issues: ${e.javaClass.simpleName}")
                addLog("Error message: ${e.message}")
                addLog("Stack trace: ${e.stackTrace.take(3).joinToString("\n") { "  at $it" }}")
                
                val errorMessage = when (e) {
                    is java.net.UnknownHostException -> "No internet connection"
                    is java.io.IOException -> "Network error: ${e.message}"
                    is com.google.gson.JsonSyntaxException -> "Data parsing error: ${e.message}"
                    else -> e.message ?: "Unknown error"
                }
                _uiState.value = currentState.copy(
                    isLoading = false,
                    error = errorMessage,
                    debugLog = debugLogBuilder.toString()
                )
            }
        }
    }
    
    fun setRepository(owner: String, repo: String) {
        addLog("Repository changed from $currentOwner/$currentRepo to $owner/$repo")
        currentOwner = owner
        currentRepo = repo
        
        // Save to preferences
        appPreferences.saveRepository(owner, repo)
        addLog("Repository settings saved to preferences")
        
        // Clear current issues when repository changes and update UI
        _uiState.value = _uiState.value.copy(
            issues = emptyList(), 
            error = null,
            currentRepository = "$currentOwner/$currentRepo"
        )
    }
    
    fun clearLog() {
        debugLogBuilder.clear()
        debugLogBuilder.append("Debug log cleared\n")
        _uiState.value = _uiState.value.copy(debugLog = debugLogBuilder.toString())
    }
    
    fun getCurrentRepository(): String = "$currentOwner/$currentRepo"
}