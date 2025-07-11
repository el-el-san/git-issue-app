package com.gitissueapp.app.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeviceFlowResponse(
    @SerialName("device_code")
    val deviceCode: String,
    @SerialName("user_code")
    val userCode: String,
    @SerialName("verification_uri")
    val verificationUri: String,
    @SerialName("expires_in")
    val expiresIn: Int,
    val interval: Int
)

@Serializable
data class AccessTokenResponse(
    @SerialName("access_token")
    val accessToken: String?,
    @SerialName("token_type")
    val tokenType: String?,
    val scope: String?,
    val error: String?,
    @SerialName("error_description")
    val errorDescription: String?
)

@Serializable
data class UserInfoResponse(
    val login: String,
    @SerialName("avatar_url")
    val avatarUrl: String,
    val name: String?,
    val email: String?
)