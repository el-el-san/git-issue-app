package com.gitissueapp.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Issue(
    val id: Long,
    val number: Int,
    val title: String,
    val body: String? = null,
    val state: String,
    val user: User,
    val labels: List<Label> = emptyList()
)

@Serializable
data class User(
    val id: Long,
    val login: String,
    val avatar_url: String? = null
)

@Serializable
data class Label(
    val id: Long,
    val name: String,
    val color: String
)