package com.gitissueapp.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gitissueapp.app.data.api.GitHubApiService
import com.gitissueapp.app.data.model.Issue
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

data class MainUiState(
    val isLoading: Boolean = false,
    val issues: List<Issue> = emptyList(),
    val error: String? = null
)

class MainViewModel : ViewModel() {
    
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState = _uiState.asStateFlow()
    
    private val apiService = createApiService()
    
    // Test repository - use a more active repository with issues
    private val testOwner = "microsoft"
    private val testRepo = "vscode"
    
    private fun createApiService(): GitHubApiService {
        val json = Json { ignoreUnknownKeys = true }
        
        // Add HTTP logging for debugging
        val loggingInterceptor = okhttp3.logging.HttpLoggingInterceptor().apply {
            level = okhttp3.logging.HttpLoggingInterceptor.Level.BODY
        }
        
        val client = okhttp3.OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
        
        return Retrofit.Builder()
            .baseUrl("https://api.github.com/")
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(GitHubApiService::class.java)
    }
    
    fun loadIssues() {
        viewModelScope.launch {
            _uiState.value = MainUiState(isLoading = true)
            
            try {
                android.util.Log.d("GitHubAPI", "Loading issues from $testOwner/$testRepo")
                val issues = apiService.getIssues(testOwner, testRepo)
                android.util.Log.d("GitHubAPI", "Loaded ${issues.size} issues")
                _uiState.value = MainUiState(issues = issues)
            } catch (e: Exception) {
                android.util.Log.e("GitHubAPI", "Error loading issues", e)
                val errorMessage = when (e) {
                    is java.net.UnknownHostException -> "No internet connection"
                    is retrofit2.HttpException -> "HTTP ${e.code()}: ${e.message()}"
                    is kotlinx.serialization.SerializationException -> "Data parsing error: ${e.message}"
                    else -> e.message ?: "Unknown error"
                }
                _uiState.value = MainUiState(error = errorMessage)
            }
        }
    }
}