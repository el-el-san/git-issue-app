package com.gitissueapp.app.data.api

import com.gitissueapp.app.data.model.Issue
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class GitHubClient {
    
    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val gson = Gson()
    
    suspend fun getIssues(owner: String, repo: String): List<Issue> = withContext(Dispatchers.IO) {
        val url = "https://api.github.com/repos/$owner/$repo/issues?state=all&per_page=30"
        
        val request = Request.Builder()
            .url(url)
            .addHeader("Accept", "application/vnd.github.v3+json")
            .addHeader("User-Agent", "GitIssueApp")
            .build()
        
        val response = httpClient.newCall(request).execute()
        
        if (!response.isSuccessful) {
            throw Exception("HTTP ${response.code}: ${response.message}")
        }
        
        val jsonString = response.body?.string() ?: throw Exception("Empty response")
        
        // Parse JSON array to List<Issue>
        val listType = object : TypeToken<List<Issue>>() {}.type
        gson.fromJson(jsonString, listType)
    }
}