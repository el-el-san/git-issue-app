package com.gitissueapp.app.data.api

import com.gitissueapp.app.data.model.Issue
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GitHubApiService {
    
    @GET("repos/{owner}/{repo}/issues")
    suspend fun getIssues(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Query("state") state: String = "all",
        @Query("per_page") perPage: Int = 30
    ): List<Issue>
}