package com.gitissueapp.app.data.model

import com.google.gson.annotations.SerializedName

data class Repository(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("full_name")
    val fullName: String,
    
    @SerializedName("description")
    val description: String?,
    
    @SerializedName("private")
    val isPrivate: Boolean,
    
    @SerializedName("fork")
    val isFork: Boolean,
    
    @SerializedName("stargazers_count")
    val starCount: Int,
    
    @SerializedName("forks_count")
    val forkCount: Int,
    
    @SerializedName("open_issues_count")
    val openIssuesCount: Int,
    
    @SerializedName("language")
    val language: String?,
    
    @SerializedName("updated_at")
    val updatedAt: String,
    
    @SerializedName("owner")
    val owner: GitHubUser
) {
    fun getDisplayName(): String {
        val languageIcon = when (language?.lowercase()) {
            "kotlin" -> "🟣"
            "java" -> "☕"
            "javascript" -> "💛"
            "typescript" -> "🔷"
            "python" -> "🐍"
            "flutter", "dart" -> "🐦"
            "swift" -> "🦉"
            "go" -> "🐹"
            "rust" -> "⚙️"
            "c++" -> "⚡"
            "c" -> "🔧"
            "php" -> "🐘"
            "ruby" -> "💎"
            "html" -> "🌐"
            "css" -> "🎨"
            "vue" -> "💚"
            "react" -> "⚛️"
            else -> "📁"
        }
        
        val issueIcon = if (openIssuesCount > 0) " 🔥$openIssuesCount" else ""
        val privateIcon = if (isPrivate) " 🔒" else ""
        
        return "$languageIcon $name$privateIcon$issueIcon"
    }
}