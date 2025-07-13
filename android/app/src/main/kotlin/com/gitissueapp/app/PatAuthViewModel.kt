package com.gitissueapp.app

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gitissueapp.app.data.api.GitHubClient
import com.gitissueapp.app.data.storage.AuthTokenStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PatAuthUiState(
    val isLoading: Boolean = false,
    val status: String = "Enter your GitHub Personal Access Token",
    val error: String? = null,
    val isTokenSaved: Boolean = false
)

class PatAuthViewModel(application: Application) : AndroidViewModel(application) {
    
    private val _uiState = MutableStateFlow(PatAuthUiState())
    val uiState = _uiState.asStateFlow()
    
    private val authTokenStorage = AuthTokenStorage(application)
    
    fun saveToken(token: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, status = "Saving token...")
        
        viewModelScope.launch {
            try {
                // Save token with Bearer type for GitHub API
                authTokenStorage.saveToken(token, "Bearer", "repo")
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    status = "Token saved successfully!",
                    isTokenSaved = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    status = "Failed to save token",
                    error = e.message
                )
            }
        }
    }
    
    fun testToken(token: String) {
        _uiState.value = _uiState.value.copy(isLoading = true, status = "Testing token...")
        
        viewModelScope.launch {
            try {
                // Create temporary storage for testing
                val tempStorage = AuthTokenStorage(getApplication())
                tempStorage.saveToken(token, "Bearer", "repo")
                
                val client = GitHubClient(tempStorage)
                
                // Test by getting user info
                val response = client.testAuthentication()
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    status = "✅ Token is valid! User: ${response.login}\nClick 'Save Token' to continue."
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    status = "❌ Token test failed",
                    error = "Token validation failed: ${e.message}"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}