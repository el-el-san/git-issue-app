package com.gitissueapp.app

import android.os.Bundle
import android.view.View
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
    private val issueAdapter = IssueAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        observeViewModel()
    }
    
    private fun setupUI() {
        // Setup RecyclerView
        binding.issuesRecyclerView.apply {
            adapter = issueAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
        
        // Setup button clicks
        binding.loadIssuesButton.setOnClickListener {
            viewModel.loadIssues()
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