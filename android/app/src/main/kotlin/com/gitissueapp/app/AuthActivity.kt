package com.gitissueapp.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.gitissueapp.app.data.auth.GitHubAuthClient
import com.gitissueapp.app.data.storage.AuthTokenStorage
import com.gitissueapp.app.databinding.ActivityAuthBinding
import kotlinx.coroutines.launch

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize dependencies
        val authClient = GitHubAuthClient()
        val tokenStorage = AuthTokenStorage(this)
        viewModel = AuthViewModel(authClient, tokenStorage)

        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        binding.authenticateButton.setOnClickListener {
            viewModel.startAuthentication()
        }

        binding.openBrowserButton.setOnClickListener {
            viewModel.uiState.value.deviceCode?.let { deviceCode ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(deviceCode.verification_uri))
                startActivity(intent)
            }
        }

        binding.copyCodeButton.setOnClickListener {
            viewModel.uiState.value.deviceCode?.let { deviceCode ->
                val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("GitHub User Code", deviceCode.user_code)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(this, "User code copied to clipboard", Toast.LENGTH_SHORT).show()
            }
        }

        binding.skipAuthButton.setOnClickListener {
            viewModel.skipAuthentication()
            navigateToMain()
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateUI(state)
                
                // Navigate to main when authenticated
                if (state.isAuthenticated && state.deviceCode != null) {
                    Toast.makeText(this@AuthActivity, "Authentication successful!", Toast.LENGTH_LONG).show()
                    navigateToMain()
                }
            }
        }
    }

    private fun updateUI(state: AuthUiState) {
        // Update status
        binding.authStatusText.text = state.status

        // Show/hide loading
        binding.loadingProgress.visibility = if (state.isLoading || state.isPolling) View.VISIBLE else View.GONE

        // Update button state
        binding.authenticateButton.isEnabled = !state.isLoading && !state.isPolling

        // Show device code if available
        state.deviceCode?.let { deviceCode ->
            binding.userCodeText.apply {
                text = deviceCode.user_code
                visibility = View.VISIBLE
            }
            binding.openBrowserButton.visibility = View.VISIBLE
            binding.copyCodeButton.visibility = View.VISIBLE
        }

        // Show error if any
        state.error?.let { error ->
            Toast.makeText(this, "Error: $error", Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.stopPolling()
    }
}