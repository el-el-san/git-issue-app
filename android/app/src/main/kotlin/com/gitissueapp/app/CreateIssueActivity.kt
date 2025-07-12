package com.gitissueapp.app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.gitissueapp.app.data.api.GitHubClient
import com.gitissueapp.app.data.storage.AuthTokenStorage
import com.gitissueapp.app.databinding.ActivityCreateIssueBinding
import kotlinx.coroutines.launch

class CreateIssueActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateIssueBinding
    private val viewModel: CreateIssueViewModel by viewModels()

    companion object {
        private const val EXTRA_OWNER = "extra_owner"
        private const val EXTRA_REPO = "extra_repo"

        fun createIntent(context: Context, owner: String, repo: String): Intent {
            return Intent(context, CreateIssueActivity::class.java).apply {
                putExtra(EXTRA_OWNER, owner)
                putExtra(EXTRA_REPO, repo)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateIssueBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize GitHub client with auth
        val authTokenStorage = AuthTokenStorage(this)
        val gitHubClient = GitHubClient(authTokenStorage)
        viewModel.initialize(gitHubClient)

        setupUI()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupUI() {
        val owner = intent.getStringExtra(EXTRA_OWNER) ?: ""
        val repo = intent.getStringExtra(EXTRA_REPO) ?: ""
        binding.repositoryText.text = "Repository: $owner/$repo"
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            finish()
        }

        binding.createButton.setOnClickListener {
            createIssue()
        }
    }

    private fun createIssue() {
        val title = binding.titleEditText.text?.toString()?.trim()
        val body = binding.bodyEditText.text?.toString()?.trim()

        if (title.isNullOrBlank()) {
            Toast.makeText(this, "Please enter an issue title", Toast.LENGTH_SHORT).show()
            return
        }

        val owner = intent.getStringExtra(EXTRA_OWNER) ?: return
        val repo = intent.getStringExtra(EXTRA_REPO) ?: return

        viewModel.createIssue(owner, repo, title, body?.ifBlank { null })
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateUI(state)
            }
        }
    }

    private fun updateUI(state: CreateIssueUiState) {
        // Loading state
        binding.loadingProgress.visibility = if (state.isLoading) View.VISIBLE else View.GONE
        binding.createButton.isEnabled = !state.isLoading

        // Error state
        if (state.error != null) {
            binding.errorText.visibility = View.VISIBLE
            binding.errorText.text = state.error
        } else {
            binding.errorText.visibility = View.GONE
        }

        // Success state
        if (state.isSuccess && state.createdIssue != null) {
            Toast.makeText(this, "Issue #${state.createdIssue.number} created successfully!", Toast.LENGTH_LONG).show()
            viewModel.clearSuccess()
            finish()
        }
    }
}