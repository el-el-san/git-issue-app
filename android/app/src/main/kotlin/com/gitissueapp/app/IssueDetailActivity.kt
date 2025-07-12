package com.gitissueapp.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gitissueapp.app.databinding.ActivityIssueDetailBinding
import kotlinx.coroutines.launch

class IssueDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityIssueDetailBinding
    private val viewModel: IssueDetailViewModel by viewModels()
    private lateinit var labelsAdapter: LabelsAdapter

    companion object {
        private const val EXTRA_OWNER = "extra_owner"
        private const val EXTRA_REPO = "extra_repo"
        private const val EXTRA_ISSUE_NUMBER = "extra_issue_number"

        fun createIntent(context: Context, owner: String, repo: String, issueNumber: Int): Intent {
            return Intent(context, IssueDetailActivity::class.java).apply {
                putExtra(EXTRA_OWNER, owner)
                putExtra(EXTRA_REPO, repo)
                putExtra(EXTRA_ISSUE_NUMBER, issueNumber)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIssueDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupClickListeners()
        observeViewModel()

        // Get intent data
        val owner = intent.getStringExtra(EXTRA_OWNER) ?: return
        val repo = intent.getStringExtra(EXTRA_REPO) ?: return
        val issueNumber = intent.getIntExtra(EXTRA_ISSUE_NUMBER, -1)
        
        if (issueNumber == -1) return

        // Load issue
        viewModel.loadIssue(owner, repo, issueNumber)
    }

    private fun setupRecyclerView() {
        labelsAdapter = LabelsAdapter()
        binding.labelsRecyclerView.apply {
            adapter = labelsAdapter
            layoutManager = LinearLayoutManager(this@IssueDetailActivity, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            finish()
        }

        binding.createIssueButton.setOnClickListener {
            val owner = intent.getStringExtra(EXTRA_OWNER) ?: return@setOnClickListener
            val repo = intent.getStringExtra(EXTRA_REPO) ?: return@setOnClickListener
            val intent = CreateIssueActivity.createIntent(this, owner, repo)
            startActivity(intent)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateUI(state)
            }
        }
    }

    private fun updateUI(state: IssueDetailUiState) {
        // Loading state
        binding.loadingProgress.visibility = if (state.isLoading) View.VISIBLE else View.GONE

        // Error state
        if (state.error != null) {
            binding.errorText.visibility = View.VISIBLE
            binding.errorText.text = state.error
        } else {
            binding.errorText.visibility = View.GONE
        }

        // Issue data
        state.issue?.let { issue ->
            binding.issueTitleText.text = issue.title
            binding.issueNumberText.text = "#${issue.number}"
            binding.issueStateText.text = issue.state.replaceFirstChar { it.uppercaseChar() }
            binding.issueAuthorText.text = "by ${issue.user.login}"
            binding.issueBodyText.text = issue.body ?: "No description provided."

            // Update labels
            labelsAdapter.updateLabels(issue.labels)
        }
    }
}