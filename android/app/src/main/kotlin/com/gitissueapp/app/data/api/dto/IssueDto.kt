package com.gitissueapp.app.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class IssueDto(
    val number: Int,
    val title: String,
    val body: String?,
    val state: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("closed_at")
    val closedAt: String?,
    val user: UserDto,
    val labels: List<LabelDto> = emptyList(),
    val comments: Int = 0
)

@Serializable
data class UserDto(
    val login: String,
    @SerialName("avatar_url")
    val avatarUrl: String
)

@Serializable
data class LabelDto(
    val name: String,
    val color: String,
    val description: String?
)

@Serializable
data class CommentDto(
    val id: Long,
    val body: String,
    val user: UserDto,
    @SerialName("created_at")
    val createdAt: String
)

@Serializable
data class CreateIssueRequest(
    val title: String,
    val body: String?,
    val labels: List<String>? = null
)

@Serializable
data class CreateCommentRequest(
    val body: String
)

@Serializable
data class UpdateLabelsRequest(
    val labels: List<String>
)