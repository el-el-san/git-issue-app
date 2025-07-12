package com.gitissueapp.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gitissueapp.app.data.api.GitHubClient
import com.gitissueapp.app.data.model.Issue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CreateIssueUiState(
    val isLoading: Boolean = false,
    val createdIssue: Issue? = null,
    val error: String? = null,
    val isSuccess: Boolean = false
)

class CreateIssueViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(CreateIssueUiState())
    val uiState = _uiState.asStateFlow()
    
    private val gitHubClient = GitHubClient()
    
    fun createIssue(owner: String, repo: String, title: String, body: String?) {
        viewModelScope.launch {
            val currentState = _uiState.value
            _uiState.value = currentState.copy(isLoading = true, error = null)
            
            try {
                val issue = gitHubClient.createIssue(owner, repo, title, body)
                _uiState.value = currentState.copy(
                    isLoading = false, 
                    createdIssue = issue,
                    isSuccess = true
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
    
    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(isSuccess = false, createdIssue = null)
    }
}