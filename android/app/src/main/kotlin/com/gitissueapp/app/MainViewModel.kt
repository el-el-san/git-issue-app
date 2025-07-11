package com.gitissueapp.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gitissueapp.app.data.api.GitHubClient
import com.gitissueapp.app.data.model.Issue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MainUiState(
    val isLoading: Boolean = false,
    val issues: List<Issue> = emptyList(),
    val error: String? = null
)

class MainViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.asStateFlow()
    
    private val gitHubClient = GitHubClient()
    
    // Default repository - use a smaller, more reliable repository  
    private var currentOwner = "octocat"
    private var currentRepo = "Hello-World"
    
    fun loadIssues() {
        viewModelScope.launch {
            _uiState.value = MainUiState(isLoading = true)
            
            try {
                android.util.Log.d("GitHubAPI", "Loading issues from $currentOwner/$currentRepo")
                val issues = gitHubClient.getIssues(currentOwner, currentRepo)
                android.util.Log.d("GitHubAPI", "Loaded ${issues.size} issues")
                _uiState.value = MainUiState(issues = issues)
            } catch (e: Exception) {
                android.util.Log.e("GitHubAPI", "Error loading issues", e)
                val errorMessage = when (e) {
                    is java.net.UnknownHostException -> "No internet connection"
                    is java.io.IOException -> "Network error: ${e.message}"
                    is com.google.gson.JsonSyntaxException -> "Data parsing error: ${e.message}"
                    else -> e.message ?: "Unknown error"
                }
                _uiState.value = MainUiState(error = errorMessage)
            }
        }
    }
    
    fun setRepository(owner: String, repo: String) {
        currentOwner = owner
        currentRepo = repo
        // Clear current issues when repository changes
        _uiState.value = MainUiState()
    }
    
    fun getCurrentRepository(): String = "$currentOwner/$currentRepo"
}