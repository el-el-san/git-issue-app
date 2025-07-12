package com.gitissueapp.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gitissueapp.app.databinding.ActivityMainBinding
import com.gitissueapp.app.ui.IssueAdapter
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var issueAdapter: IssueAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        observeViewModel()
    }
    
    private fun setupUI() {
        // Setup RecyclerView with click handler
        issueAdapter = IssueAdapter { issue ->
            openIssueDetail(issue)
        }
        binding.issuesRecyclerView.apply {
            adapter = issueAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
        
        // Setup button clicks
        binding.loadIssuesButton.setOnClickListener {
            viewModel.loadIssues()
        }
        
        binding.createIssueButton.setOnClickListener {
            openCreateIssue()
        }
        
        binding.setRepositoryButton.setOnClickListener {
            val repoText = binding.repositoryInput.text.toString().trim()
            if (repoText.contains("/") && repoText.split("/").size == 2) {
                val parts = repoText.split("/")
                viewModel.setRepository(parts[0], parts[1])
                binding.statusText.text = "Repository set to $repoText\nTap 'Load Issues' to fetch data"
            } else {
                binding.statusText.text = "❌ Invalid format. Use: owner/repository"
            }
        }
        
        // Debug log buttons
        binding.copyLogButton.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Debug Log", binding.debugLogText.text)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Debug log copied to clipboard", Toast.LENGTH_SHORT).show()
        }
        
        binding.clearLogButton.setOnClickListener {
            viewModel.clearLog()
        }
    }
    
    private fun openIssueDetail(issue: Issue) {
        val repoText = viewModel.getCurrentRepository()
        val parts = repoText.split("/")
        if (parts.size == 2) {
            val intent = IssueDetailActivity.createIntent(this, parts[0], parts[1], issue.number)
            startActivity(intent)
        }
    }
    
    private fun openCreateIssue() {
        val repoText = viewModel.getCurrentRepository()
        val parts = repoText.split("/")
        if (parts.size == 2) {
            val intent = CreateIssueActivity.createIntent(this, parts[0], parts[1])
            startActivity(intent)
        } else {
            Toast.makeText(this, "Please set a valid repository first", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                // Update status text
                binding.statusText.text = when {
                    state.isLoading -> "🔄 Loading issues from GitHub..."
                    state.error != null -> "❌ Error: ${state.error}"
                    state.issues.isNotEmpty() -> "✅ Found ${state.issues.size} issues"
                    else -> "🎉 GitHub Issue Manager\n\nTap 'Load Issues' to fetch data"
                }
                
                // Update UI visibility
                binding.loadIssuesButton.isEnabled = !state.isLoading
                
                // Show debug log when there's content or errors
                if (state.debugLog.isNotEmpty() || state.error != null) {
                    binding.debugSection.visibility = View.VISIBLE
                    binding.debugLogText.text = state.debugLog
                } else {
                    binding.debugSection.visibility = View.GONE
                }
                
                if (state.issues.isNotEmpty()) {
                    binding.issuesRecyclerView.visibility = View.VISIBLE
                    binding.statusText.visibility = View.GONE
                    issueAdapter.submitList(state.issues)
                } else {
                    binding.issuesRecyclerView.visibility = View.GONE
                    binding.statusText.visibility = View.VISIBLE
                }
            }
        }
    }
}