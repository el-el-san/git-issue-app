package com.gitissueapp.app.data.model

data class GitHubUser(
    val login: String,
    val id: Long,
    val avatar_url: String? = null,
    val name: String? = null,
    val email: String? = null
)