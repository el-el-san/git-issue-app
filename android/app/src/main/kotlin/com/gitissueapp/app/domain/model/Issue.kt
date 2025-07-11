package com.gitissueapp.app.domain.model

import java.time.LocalDateTime

data class Issue(
    val number: Int,
    val title: String,
    val body: String?,
    val state: IssueState,
    val createdAt: LocalDateTime,
    val closedAt: LocalDateTime?,
    val user: User,
    val labels: List<Label>,
    val comments: Int
)

enum class IssueState {
    OPEN, CLOSED
}

data class User(
    val login: String,
    val avatarUrl: String
)

data class Label(
    val name: String,
    val color: String,
    val description: String?
)

data class Comment(
    val id: Long,
    val body: String,
    val user: User,
    val createdAt: LocalDateTime
)