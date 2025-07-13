package com.gitissueapp.app.data.auth

import com.gitissueapp.app.data.model.AccessTokenResponse
import com.gitissueapp.app.data.model.AuthError
import com.gitissueapp.app.data.model.DeviceCodeResponse
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

class GitHubAuthClient {
    
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val gson = Gson()
    
    // GitHub App client ID (public, safe to include)
    private val clientId = "Ov23liI4dT9YdYF7iUGf"
    
    suspend fun requestDeviceCode(): DeviceCodeResponse = withContext(Dispatchers.IO) {
        val formBody = "client_id=$clientId&scope=repo"
        
        val request = Request.Builder()
            .url("https://github.com/login/device/code")
            .addHeader("Accept", "application/json")
            .addHeader("User-Agent", "GitIssueApp")
            .post(formBody.toRequestBody("application/x-www-form-urlencoded".toMediaType()))
            .build()
        
        val response = httpClient.newCall(request).execute()
        
        if (!response.isSuccessful) {
            throw Exception("HTTP ${response.code}: ${response.message}")
        }
        
        val jsonString = response.body?.string() ?: throw Exception("Empty response")
        gson.fromJson(jsonString, DeviceCodeResponse::class.java)
    }
    
    suspend fun pollForAccessToken(deviceCode: String): AccessTokenResponse = withContext(Dispatchers.IO) {
        val formBody = "client_id=$clientId&device_code=$deviceCode&grant_type=urn:ietf:params:oauth:grant-type:device_code"
        
        val request = Request.Builder()
            .url("https://github.com/login/oauth/access_token")
            .addHeader("Accept", "application/json")
            .addHeader("User-Agent", "GitIssueApp")
            .post(formBody.toRequestBody("application/x-www-form-urlencoded".toMediaType()))
            .build()
        
        val response = httpClient.newCall(request).execute()
        
        if (!response.isSuccessful) {
            throw Exception("HTTP ${response.code}: ${response.message}")
        }
        
        val jsonString = response.body?.string() ?: throw Exception("Empty response")
        
        // Check for error first
        try {
            val error = gson.fromJson(jsonString, AuthError::class.java)
            if (error.error.isNotEmpty()) {
                when (error.error) {
                    "authorization_pending" -> throw AuthPendingException("User hasn't authorized yet")
                    "slow_down" -> throw SlowDownException("Polling too fast")
                    "expired_token" -> throw ExpiredTokenException("Device code expired")
                    "access_denied" -> throw AccessDeniedException("User denied access")
                    else -> throw Exception("Auth error: ${error.error}")
                }
            }
        } catch (e: Exception) {
            if (e is AuthException) throw e
            // Continue if JSON parsing failed (might be success response)
        }
        
        gson.fromJson(jsonString, AccessTokenResponse::class.java)
    }
}

// Custom exceptions for different auth states
sealed class AuthException(message: String) : Exception(message)
class AuthPendingException(message: String) : AuthException(message)
class SlowDownException(message: String) : AuthException(message)
class ExpiredTokenException(message: String) : AuthException(message)
class AccessDeniedException(message: String) : AuthException(message)