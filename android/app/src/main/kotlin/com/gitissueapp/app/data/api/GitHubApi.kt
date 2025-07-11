package com.gitissueapp.app.data.api

import com.gitissueapp.app.data.api.dto.*
import retrofit2.http.*

interface GitHubApi {
    
    // Authentication
    @FormUrlEncoded
    @POST("https://github.com/login/device/code")
    @Headers("Accept: application/json")
    suspend fun initiateDeviceFlow(
        @Field("client_id") clientId: String,
        @Field("scope") scope: String = "repo"
    ): DeviceFlowResponse

    @FormUrlEncoded
    @POST("https://github.com/login/oauth/access_token")
    @Headers("Accept: application/json")
    suspend fun pollAccessToken(
        @Field("client_id") clientId: String,
        @Field("device_code") deviceCode: String,
        @Field("grant_type") grantType: String = "urn:ietf:params:oauth:grant-type:device_code"
    ): AccessTokenResponse

    @GET("user")
    suspend fun getUserInfo(): UserInfoResponse

    // Issues
    @GET("repos/{owner}/{repo}/issues")
    suspend fun getIssues(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Query("state") state: String = "all",
        @Query("page") page: Int = 1,
        @Query("per_page") perPage: Int = 30
    ): List<IssueDto>

    @GET("repos/{owner}/{repo}/issues/{issue_number}")
    suspend fun getIssue(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("issue_number") issueNumber: Int
    ): IssueDto

    @POST("repos/{owner}/{repo}/issues")
    suspend fun createIssue(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Body request: CreateIssueRequest
    ): IssueDto

    // Comments
    @GET("repos/{owner}/{repo}/issues/{issue_number}/comments")
    suspend fun getIssueComments(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("issue_number") issueNumber: Int
    ): List<CommentDto>

    @POST("repos/{owner}/{repo}/issues/{issue_number}/comments")
    suspend fun createComment(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("issue_number") issueNumber: Int,
        @Body request: CreateCommentRequest
    ): CommentDto

    // Labels
    @GET("repos/{owner}/{repo}/labels")
    suspend fun getLabels(
        @Path("owner") owner: String,
        @Path("repo") repo: String
    ): List<LabelDto>

    @PATCH("repos/{owner}/{repo}/issues/{issue_number}")
    suspend fun updateIssueLabels(
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path("issue_number") issueNumber: Int,
        @Body request: UpdateLabelsRequest
    ): IssueDto
}