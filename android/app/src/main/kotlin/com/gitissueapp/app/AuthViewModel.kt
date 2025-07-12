package com.gitissueapp.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gitissueapp.app.data.auth.AuthPendingException
import com.gitissueapp.app.data.auth.GitHubAuthClient
import com.gitissueapp.app.data.auth.SlowDownException
import com.gitissueapp.app.data.model.DeviceCodeResponse
import com.gitissueapp.app.data.storage.AuthTokenStorage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val deviceCode: DeviceCodeResponse? = null,
    val isPolling: Boolean = false,
    val error: String? = null,
    val isAuthenticated: Boolean = false,
    val status: String = "Ready to authenticate"
)

class AuthViewModel(
    private val authClient: GitHubAuthClient,
    private val tokenStorage: AuthTokenStorage
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()
    
    init {
        // Check if already authenticated
        if (tokenStorage.isAuthenticated()) {
            _uiState.value = _uiState.value.copy(
                isAuthenticated = true,
                status = "Already authenticated"
            )
        }
    }
    
    fun startAuthentication() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null,
                status = "Requesting device code..."
            )
            
            try {
                val deviceCode = authClient.requestDeviceCode()
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    deviceCode = deviceCode,
                    status = "Please enter the code in your browser and authorize the app"
                )
                
                // Start polling for access token
                startPolling(deviceCode.device_code, deviceCode.interval)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to start authentication",
                    status = "Authentication failed"
                )
            }
        }
    }
    
    private fun startPolling(deviceCode: String, interval: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPolling = true)
            
            while (_uiState.value.isPolling && !_uiState.value.isAuthenticated) {
                try {
                    delay((interval * 1000).toLong()) // Convert to milliseconds
                    
                    val tokenResponse = authClient.pollForAccessToken(deviceCode)
                    
                    // Save token
                    tokenStorage.saveToken(
                        tokenResponse.access_token,
                        tokenResponse.token_type,
                        tokenResponse.scope
                    )
                    
                    _uiState.value = _uiState.value.copy(
                        isPolling = false,
                        isAuthenticated = true,
                        status = "Successfully authenticated!"
                    )
                    
                } catch (e: AuthPendingException) {
                    // User hasn't authorized yet, continue polling
                    _uiState.value = _uiState.value.copy(
                        status = "Waiting for authorization..."
                    )
                } catch (e: SlowDownException) {
                    // Slow down polling
                    delay(5000) // Wait extra 5 seconds
                    _uiState.value = _uiState.value.copy(
                        status = "Slowing down polling..."
                    )
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isPolling = false,
                        error = e.message ?: "Authentication failed",
                        status = "Authentication failed"
                    )
                    break
                }
            }
        }
    }
    
    fun stopPolling() {
        _uiState.value = _uiState.value.copy(isPolling = false)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun skipAuthentication() {
        _uiState.value = _uiState.value.copy(
            isAuthenticated = false,
            status = "Skipped authentication - public access only"
        )
    }
}