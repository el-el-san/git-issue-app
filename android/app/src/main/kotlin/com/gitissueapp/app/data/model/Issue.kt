package com.gitissueapp.app.data.model

import com.google.gson.annotations.SerializedName

data class Issue(
    val id: Long,
    val number: Int,
    val title: String,
    val body: String? = null,
    val state: String,
    val user: User,
    val labels: List<Label> = emptyList(),
    @SerializedName("created_at") val createdAt: String? = null,
    @SerializedName("updated_at") val updatedAt: String? = null
)

data class User(
    val id: Long,
    val login: String,
    @SerializedName("avatar_url") val avatarUrl: String? = null,
    val type: String? = null
)

data class Label(
    val id: Long,
    val name: String,
    val color: String,
    val description: String? = null
)