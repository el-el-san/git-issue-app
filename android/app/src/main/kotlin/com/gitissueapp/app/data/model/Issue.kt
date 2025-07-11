package com.gitissueapp.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Issue(
    val id: Long,
    val number: Int,
    val title: String,
    val body: String? = null,
    val state: String,
    val user: User,
    val labels: List<Label> = emptyList(),
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null
)

@Serializable
data class User(
    val id: Long,
    val login: String,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val type: String? = null
)

@Serializable
data class Label(
    val id: Long,
    val name: String,
    val color: String,
    val description: String? = null
)