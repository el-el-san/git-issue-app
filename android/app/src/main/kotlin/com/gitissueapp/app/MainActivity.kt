package com.gitissueapp.app

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.gitissueapp.app.databinding.ActivityMainBinding
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupUI()
        observeViewModel()
    }
    
    private fun setupUI() {
        binding.welcomeText.text = "🎉 GitHub Issue Manager\n\nPhase 2: API Integration Ready!"
        
        binding.nextPhaseButton.setOnClickListener {
            // Test GitHub API
            viewModel.testGitHubApi()
        }
    }
    
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.welcomeText.text = when {
                    state.isLoading -> "🔄 Testing GitHub API..."
                    state.error != null -> "❌ Error: ${state.error}"
                    state.issueCount > 0 -> "✅ Success! Found ${state.issueCount} issues\n\nGitHub API is working!"
                    else -> "🎉 GitHub Issue Manager\n\nPhase 2: API Integration Ready!"
                }
                
                binding.nextPhaseButton.isEnabled = !state.isLoading
            }
        }
    }
}