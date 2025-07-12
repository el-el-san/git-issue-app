package com.gitissueapp.app.data.api

import com.gitissueapp.app.data.model.Issue
import com.gitissueapp.app.data.storage.AuthTokenStorage
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.MediaType.Companion.toMediaType
import java.util.concurrent.TimeUnit

class GitHubClient(private val authTokenStorage: AuthTokenStorage? = null) {
    
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val gson = Gson()
    
    private fun Request.Builder.addAuthHeader(): Request.Builder {
        authTokenStorage?.getAuthorizationHeader()?.let { authHeader ->
            addHeader("Authorization", authHeader)
        }
        return this
    }
    
    suspend fun getIssues(owner: String, repo: String): List<Issue> = withContext(Dispatchers.IO) {
        val url = "https://api.github.com/repos/$owner/$repo/issues?state=all&per_page=30"
        
        val request = Request.Builder()
            .url(url)
            .addHeader("Accept", "application/vnd.github.v3+json")
            .addHeader("User-Agent", "GitIssueApp")
            .addAuthHeader()
            .build()
        
        val response = httpClient.newCall(request).execute()
        
        if (!response.isSuccessful) {
            throw Exception("HTTP ${response.code}: ${response.message}")
        }
        
        val jsonString = response.body?.string() ?: throw Exception("Empty response")
        
        // Parse JSON array manually without TypeToken
        val jsonArray = JsonParser.parseString(jsonString).asJsonArray
        val issues = mutableListOf<Issue>()
        
        for (jsonElement in jsonArray) {
            try {
                val issue = gson.fromJson(jsonElement, Issue::class.java)
                issues.add(issue)
            } catch (e: Exception) {
                // Skip problematic issues and continue
                android.util.Log.w("GitHubClient", "Failed to parse issue: ${e.message}")
            }
        }
        
        issues
    }
    
    suspend fun getIssue(owner: String, repo: String, number: Int): Issue = withContext(Dispatchers.IO) {
        val url = "https://api.github.com/repos/$owner/$repo/issues/$number"
        
        val request = Request.Builder()
            .url(url)
            .addHeader("Accept", "application/vnd.github.v3+json")
            .addHeader("User-Agent", "GitIssueApp")
            .addAuthHeader()
            .build()
        
        val response = httpClient.newCall(request).execute()
        
        if (!response.isSuccessful) {
            throw Exception("HTTP ${response.code}: ${response.message}")
        }
        
        val jsonString = response.body?.string() ?: throw Exception("Empty response")
        gson.fromJson(jsonString, Issue::class.java)
    }
    
    suspend fun createIssue(owner: String, repo: String, title: String, body: String?): Issue = withContext(Dispatchers.IO) {
        val url = "https://api.github.com/repos/$owner/$repo/issues"
        
        val requestBody = mutableMapOf<String, Any>("title" to title)
        if (!body.isNullOrBlank()) {
            requestBody["body"] = body
        }
        
        val requestBodyString = gson.toJson(requestBody)
        
        val request = Request.Builder()
            .url(url)
            .addHeader("Accept", "application/vnd.github.v3+json")
            .addHeader("Content-Type", "application/json")
            .addHeader("User-Agent", "GitIssueApp")
            .addAuthHeader()
            .post(okhttp3.RequestBody.create(
                "application/json".toMediaType(), 
                requestBodyString
            ))
            .build()
        
        val response = httpClient.newCall(request).execute()
        
        if (!response.isSuccessful) {
            throw Exception("HTTP ${response.code}: ${response.message}")
        }
        
        val jsonString = response.body?.string() ?: throw Exception("Empty response")
        gson.fromJson(jsonString, Issue::class.java)
    }
}