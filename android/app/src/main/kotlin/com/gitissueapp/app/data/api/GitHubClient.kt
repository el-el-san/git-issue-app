package com.gitissueapp.app.data.api

import com.gitissueapp.app.data.model.Issue
import com.gitissueapp.app.data.model.GitHubUser
import com.gitissueapp.app.data.model.Repository
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
        val authHeader = authTokenStorage?.getAuthorizationHeader()
        android.util.Log.d("GitHubClient", "Auth header being added: $authHeader")
        
        if (authHeader != null) {
            addHeader("Authorization", authHeader)
        } else {
            android.util.Log.w("GitHubClient", "No auth header available - request will be unauthenticated")
        }
        return this
    }
    
    suspend fun getIssues(owner: String, repo: String): List<Issue> = withContext(Dispatchers.IO) {
        val url = "https://api.github.com/repos/$owner/$repo/issues?state=all&per_page=100&sort=created&direction=desc"
        
        android.util.Log.d("GitHubClient", "Fetching issues from: $url")
        
        val request = Request.Builder()
            .url(url)
            .addHeader("Accept", "application/vnd.github+json")
            .addHeader("User-Agent", "GitIssueApp")
            .addHeader("X-GitHub-Api-Version", "2022-11-28")
            .addAuthHeader()
            .build()
        
        val response = httpClient.newCall(request).execute()
        
        if (!response.isSuccessful) {
            throw Exception("HTTP ${response.code}: ${response.message}")
        }
        
        val jsonString = response.body?.string() ?: throw Exception("Empty response")
        android.util.Log.d("GitHubClient", "Response body length: ${jsonString.length}")
        android.util.Log.d("GitHubClient", "Response body preview: ${jsonString.take(200)}...")
        
        // Parse JSON array manually without TypeToken
        val jsonArray = JsonParser.parseString(jsonString).asJsonArray
        val issues = mutableListOf<Issue>()
        
        android.util.Log.d("GitHubClient", "JSON array size: ${jsonArray.size()}")
        
        for ((index, jsonElement) in jsonArray.withIndex()) {
            try {
                val issue = gson.fromJson(jsonElement, Issue::class.java)
                issues.add(issue)
                android.util.Log.d("GitHubClient", "Parsed issue $index: #${issue.number} ${issue.title}")
            } catch (e: Exception) {
                // Skip problematic issues and continue
                android.util.Log.w("GitHubClient", "Failed to parse issue $index: ${e.message}")
            }
        }
        
        android.util.Log.d("GitHubClient", "Successfully parsed ${issues.size} issues")
        issues
    }
    
    suspend fun getIssue(owner: String, repo: String, number: Int): Issue = withContext(Dispatchers.IO) {
        val url = "https://api.github.com/repos/$owner/$repo/issues/$number"
        
        val request = Request.Builder()
            .url(url)
            .addHeader("Accept", "application/vnd.github+json")
            .addHeader("User-Agent", "GitIssueApp")
            .addHeader("X-GitHub-Api-Version", "2022-11-28")
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
        android.util.Log.d("GitHubClient", "Creating issue with URL: $url")
        android.util.Log.d("GitHubClient", "Request body: $requestBodyString")
        
        val request = Request.Builder()
            .url(url)
            .addHeader("Accept", "application/vnd.github+json")
            .addHeader("Content-Type", "application/json")
            .addHeader("User-Agent", "GitIssueApp")
            .addHeader("X-GitHub-Api-Version", "2022-11-28")
            .addAuthHeader()
            .post(okhttp3.RequestBody.create(
                "application/json".toMediaType(), 
                requestBodyString
            ))
            .build()
        
        val response = httpClient.newCall(request).execute()
        
        if (!response.isSuccessful) {
            val errorBody = response.body?.string() ?: "No error details"
            android.util.Log.e("GitHubClient", "Create issue failed: HTTP ${response.code}, Body: $errorBody")
            throw Exception("HTTP ${response.code}: ${response.message}. Details: $errorBody")
        }
        
        val jsonString = response.body?.string() ?: throw Exception("Empty response")
        gson.fromJson(jsonString, Issue::class.java)
    }
    
    suspend fun testAuthentication(): GitHubUser = withContext(Dispatchers.IO) {
        val url = "https://api.github.com/user"
        
        val request = Request.Builder()
            .url(url)
            .addHeader("Accept", "application/vnd.github+json")
            .addHeader("User-Agent", "GitIssueApp")
            .addHeader("X-GitHub-Api-Version", "2022-11-28")
            .addAuthHeader()
            .build()
        
        val response = httpClient.newCall(request).execute()
        
        if (!response.isSuccessful) {
            val errorBody = response.body?.string() ?: "No error details"
            android.util.Log.e("GitHubClient", "Auth test failed: HTTP ${response.code}, Body: $errorBody")
            throw Exception("Authentication failed: HTTP ${response.code}. ${if (response.code == 401) "Invalid token" else errorBody}")
        }
        
        val jsonString = response.body?.string() ?: throw Exception("Empty response")
        gson.fromJson(jsonString, GitHubUser::class.java)
    }
    
    suspend fun getUserRepositories(username: String): List<Repository> = withContext(Dispatchers.IO) {
        val url = "https://api.github.com/users/$username/repos?sort=updated&per_page=100"
        
        android.util.Log.d("GitHubClient", "Fetching repositories for user: $username")
        
        val request = Request.Builder()
            .url(url)
            .addHeader("Accept", "application/vnd.github+json")
            .addHeader("User-Agent", "GitIssueApp")
            .addHeader("X-GitHub-Api-Version", "2022-11-28")
            .addAuthHeader()
            .build()
        
        val response = httpClient.newCall(request).execute()
        
        if (!response.isSuccessful) {
            throw Exception("HTTP ${response.code}: ${response.message}")
        }
        
        val jsonString = response.body?.string() ?: throw Exception("Empty response")
        
        // Parse JSON array manually
        val jsonArray = JsonParser.parseString(jsonString).asJsonArray
        val repositories = mutableListOf<Repository>()
        
        android.util.Log.d("GitHubClient", "Found ${jsonArray.size()} repositories for $username")
        
        for ((index, jsonElement) in jsonArray.withIndex()) {
            try {
                val repository = gson.fromJson(jsonElement, Repository::class.java)
                repositories.add(repository)
                android.util.Log.d("GitHubClient", "Parsed repo $index: ${repository.fullName}")
            } catch (e: Exception) {
                android.util.Log.w("GitHubClient", "Failed to parse repository $index: ${e.message}")
            }
        }
        
        android.util.Log.d("GitHubClient", "Successfully parsed ${repositories.size} repositories")
        repositories
    }
    
    suspend fun getAuthenticatedUserRepositories(): List<Repository> = withContext(Dispatchers.IO) {
        val url = "https://api.github.com/user/repos?sort=updated&per_page=100&affiliation=owner,collaborator"
        
        android.util.Log.d("GitHubClient", "Fetching authenticated user repositories")
        
        val request = Request.Builder()
            .url(url)
            .addHeader("Accept", "application/vnd.github+json")
            .addHeader("User-Agent", "GitIssueApp")
            .addHeader("X-GitHub-Api-Version", "2022-11-28")
            .addAuthHeader()
            .build()
        
        val response = httpClient.newCall(request).execute()
        
        if (!response.isSuccessful) {
            throw Exception("HTTP ${response.code}: ${response.message}")
        }
        
        val jsonString = response.body?.string() ?: throw Exception("Empty response")
        
        // Parse JSON array manually
        val jsonArray = JsonParser.parseString(jsonString).asJsonArray
        val repositories = mutableListOf<Repository>()
        
        android.util.Log.d("GitHubClient", "Found ${jsonArray.size()} repositories for authenticated user")
        
        for ((index, jsonElement) in jsonArray.withIndex()) {
            try {
                val repository = gson.fromJson(jsonElement, Repository::class.java)
                repositories.add(repository)
                android.util.Log.d("GitHubClient", "Parsed repo $index: ${repository.fullName}")
            } catch (e: Exception) {
                android.util.Log.w("GitHubClient", "Failed to parse repository $index: ${e.message}")
            }
        }
        
        android.util.Log.d("GitHubClient", "Successfully parsed ${repositories.size} repositories")
        repositories
    }
}