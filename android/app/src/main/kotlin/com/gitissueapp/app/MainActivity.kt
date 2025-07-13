package com.gitissueapp.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gitissueapp.app.databinding.ActivityMainBinding
import com.gitissueapp.app.data.model.Issue
import com.gitissueapp.app.ui.IssueAdapter
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(this@MainActivity) as T
            }
        }
    }
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
        
        // Setup repository dropdown with el-el-san repositories
        val repositories = arrayOf(
            "el-el-san/git-issue-app",
            "el-el-san/portfolio-website",
            "el-el-san/react-dashboard",
            "el-el-san/kotlin-projects",
            "el-el-san/flutter-apps",
            "el-el-san/vue-components",
            "el-el-san/node-api",
            "el-el-san/python-tools",
            "el-el-san/docker-configs"
        )
        
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, repositories)
        binding.repositoryDropdown.setAdapter(adapter)
        
        binding.repositoryDropdown.setOnItemClickListener { _, _, position, _ ->
            val selectedRepo = repositories[position]
            val parts = selectedRepo.split("/")
            if (parts.size == 2) {
                viewModel.setRepository(parts[0], parts[1])
                binding.repositoryInput.setText(selectedRepo)
                binding.statusText.text = "✅ Repository set to $selectedRepo\nTap 'Load Issues' to fetch data"
            }
        }
        
        // Setup button clicks
        binding.loadIssuesButton.setOnClickListener {
            viewModel.loadIssues()
        }
        
        binding.authButton.setOnClickListener {
            openAuthActivity()
        }
        
        binding.createIssueButton.setOnClickListener {
            openCreateIssue()
        }
        
        binding.setRepositoryButton.setOnClickListener {
            val repoText = binding.repositoryInput.text.toString().trim()
            if (repoText.contains("/") && repoText.split("/").size == 2) {
                val parts = repoText.split("/")
                viewModel.setRepository(parts[0], parts[1])
                binding.statusText.text = "✅ Repository set to $repoText\nTap 'Load Issues' to fetch data"
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
    
    private fun openAuthActivity() {
        val intent = Intent(this, PatAuthActivity::class.java)
        startActivity(intent)
    }
    
    private fun openCreateIssue() {
        // Check authentication first
        val authTokenStorage = com.gitissueapp.app.data.storage.AuthTokenStorage(this)
        if (!authTokenStorage.isAuthenticated()) {
            Toast.makeText(this, "Please authenticate with GitHub first", Toast.LENGTH_LONG).show()
            openAuthActivity()
            return
        }
        
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
                // Update repository input with saved value
                if (state.currentRepository.isNotEmpty() && binding.repositoryInput.text.toString().isEmpty()) {
                    binding.repositoryInput.setText(state.currentRepository)
                    binding.repositoryDropdown.setText(state.currentRepository, false)
                }
                
                // Update status text
                val authStatus = if (state.isAuthenticated) "🔒 Authenticated" else "❌ Not Authenticated"
                binding.statusText.text = when {
                    state.isLoading -> "🔄 Loading issues from ${state.currentRepository}..."
                    state.error != null -> "❌ Error: ${state.error}\n\n$authStatus"
                    state.issues.isNotEmpty() -> "✅ Found ${state.issues.size} issues from ${state.currentRepository}\n$authStatus"
                    state.currentRepository.isNotEmpty() -> "📁 Repository: ${state.currentRepository}\n$authStatus\n\nTap 'Load Issues' to fetch data"
                    else -> "🎉 GitHub Issue Manager\n$authStatus\n\nSet a repository and authenticate"
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