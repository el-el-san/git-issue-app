package com.gitissueapp.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.gitissueapp.app.databinding.ActivityPatAuthBinding
import kotlinx.coroutines.launch

class PatAuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPatAuthBinding
    private val viewModel: PatAuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.backButton.setOnClickListener {
            finish()
        }

        binding.openGitHubButton.setOnClickListener {
            openGitHubTokenPage()
        }

        binding.saveTokenButton.setOnClickListener {
            saveToken()
        }

        binding.testTokenButton.setOnClickListener {
            testToken()
        }
    }

    private fun openGitHubTokenPage() {
        val url = "https://github.com/settings/tokens/new?scopes=repo&description=GitIssueApp"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Could not open browser", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveToken() {
        val token = binding.tokenEditText.text?.toString()?.trim()
        if (token.isNullOrBlank()) {
            Toast.makeText(this, "Please enter a valid token", Toast.LENGTH_SHORT).show()
            return
        }

        if (!token.startsWith("ghp_") && !token.startsWith("github_pat_")) {
            Toast.makeText(this, "Token should start with 'ghp_' or 'github_pat_'", Toast.LENGTH_LONG).show()
            return
        }

        viewModel.saveToken(token)
    }

    private fun testToken() {
        val token = binding.tokenEditText.text?.toString()?.trim()
        if (token.isNullOrBlank()) {
            Toast.makeText(this, "Please enter a token first", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.testToken(token)
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateUI(state)
            }
        }
    }

    private fun updateUI(state: PatAuthUiState) {
        // Loading state
        binding.loadingProgress.visibility = if (state.isLoading) View.VISIBLE else View.GONE
        binding.saveTokenButton.isEnabled = !state.isLoading
        binding.testTokenButton.isEnabled = !state.isLoading

        // Status text
        binding.statusText.text = state.status

        // Show error if any
        state.error?.let { error ->
            Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }

        // Success - token saved
        if (state.isTokenSaved) {
            Toast.makeText(this, "Token saved successfully!", Toast.LENGTH_SHORT).show()
            navigateToMain()
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}