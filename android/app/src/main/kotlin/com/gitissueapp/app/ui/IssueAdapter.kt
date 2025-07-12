package com.gitissueapp.app.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gitissueapp.app.data.model.Issue
import com.gitissueapp.app.databinding.ItemIssueBinding

class IssueAdapter(
    private val onIssueClick: (Issue) -> Unit = {}
) : ListAdapter<Issue, IssueAdapter.IssueViewHolder>(IssueDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IssueViewHolder {
        val binding = ItemIssueBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return IssueViewHolder(binding, onIssueClick)
    }

    override fun onBindViewHolder(holder: IssueViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class IssueViewHolder(
        private val binding: ItemIssueBinding,
        private val onIssueClick: (Issue) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(issue: Issue) {
            binding.apply {
                issueNumber.text = "#${issue.number}"
                issueTitle.text = issue.title
                issueBody.text = when {
                    issue.body.isNullOrBlank() -> "No description provided"
                    issue.body.length > 150 -> issue.body.take(150) + "..."
                    else -> issue.body
                }
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
                
                // Set click listener
                root.setOnClickListener {
                    onIssueClick(issue)
                }
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