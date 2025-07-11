package com.gitissueapp.app.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gitissueapp.app.data.model.Issue
import com.gitissueapp.app.databinding.ItemIssueBinding

class IssueAdapter : ListAdapter<Issue, IssueAdapter.IssueViewHolder>(IssueDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IssueViewHolder {
        val binding = ItemIssueBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return IssueViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IssueViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class IssueViewHolder(private val binding: ItemIssueBinding) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(issue: Issue) {
            binding.apply {
                issueNumber.text = "#${issue.number}"
                issueTitle.text = issue.title
                issueBody.text = issue.body ?: "No description"
                issueAuthor.text = "by @${issue.user.login}"
                
                // Set state color
                issueState.text = issue.state
                issueState.setBackgroundColor(
                    when (issue.state.lowercase()) {
                        "open" -> Color.parseColor("#22c55e") // Green
                        "closed" -> Color.parseColor("#ef4444") // Red
                        else -> Color.parseColor("#6b7280") // Gray
                    }
                )
            }
        }
    }
}

class IssueDiffCallback : DiffUtil.ItemCallback<Issue>() {
    override fun areItemsTheSame(oldItem: Issue, newItem: Issue): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Issue, newItem: Issue): Boolean {
        return oldItem == newItem
    }
}