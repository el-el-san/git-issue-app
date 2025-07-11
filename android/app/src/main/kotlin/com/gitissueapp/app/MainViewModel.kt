package com.gitissueapp.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gitissueapp.app.data.api.GitHubApiService
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

data class MainUiState(
    val isLoading: Boolean = false,
    val issueCount: Int = 0,
    val error: String? = null
)

class MainViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.asStateFlow()
    
    private val apiService = createApiService()
    
    private fun createApiService(): GitHubApiService {
        val json = Json { ignoreUnknownKeys = true }
        
        return Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(GitHubApiService::class.java)
    }
    
    fun testGitHubApi() {
        viewModelScope.launch {
            _uiState.value = MainUiState(isLoading = true)
            
            try {
                // Test with a public repository
                val issues = apiService.getIssues("octocat", "Hello-World")
                _uiState.value = MainUiState(issueCount = issues.size)
            } catch (e: Exception) {
                _uiState.value = MainUiState(error = e.message ?: "Unknown error")
            }
        }
    }
}