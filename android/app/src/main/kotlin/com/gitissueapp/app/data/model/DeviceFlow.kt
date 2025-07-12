package com.gitissueapp.app.data.model

data class DeviceCodeResponse(
    val device_code: String,
    val user_code: String,
    val verification_uri: String,
    val expires_in: Int,
    val interval: Int
)

data class AccessTokenResponse(
    val access_token: String,
    val token_type: String,
    val scope: String
)

data class AuthError(
    val error: String,
    val error_description: String? = null
)