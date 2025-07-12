package com.gitissueapp.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gitissueapp.app.data.api.GitHubClient
import com.gitissueapp.app.data.model.Issue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class IssueDetailUiState(
    val isLoading: Boolean = false,
    val issue: Issue? = null,
    val error: String? = null
)

class IssueDetailViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(IssueDetailUiState())
    val uiState = _uiState.asStateFlow()
    
    private var gitHubClient: GitHubClient? = null
    
    fun initialize(gitHubClient: GitHubClient) {
        this.gitHubClient = gitHubClient
    }
    
    fun loadIssue(owner: String, repo: String, number: Int) {
        viewModelScope.launch {
            val currentState = _uiState.value
            _uiState.value = currentState.copy(isLoading = true, error = null)
            
            try {
                val issue = gitHubClient?.getIssue(owner, repo, number) ?: throw Exception("GitHubClient not initialized")
                _uiState.value = currentState.copy(
                    isLoading = false, 
                    issue = issue
                )
            } catch (e: Exception) {
                val errorMessage = when (e) {
                    is java.net.UnknownHostException -> "No internet connection"
                    is java.io.IOException -> "Network error: ${e.message}"
                    is com.google.gson.JsonSyntaxException -> "Data parsing error: ${e.message}"
                    else -> e.message ?: "Unknown error"
                }
                _uiState.value = currentState.copy(
                    isLoading = false,
                    error = errorMessage
                )
            }
        }
    }
}