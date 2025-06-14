import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:git_issue_app/src/providers/auth_provider.dart';
import 'package:git_issue_app/src/theme/app_theme.dart';

class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final _tokenController = TextEditingController();
  bool _isTokenVisible = false;
  bool _isDeviceFlowActive = false;
  String? _deviceCode;
  String? _userCode;
  String? _verificationUri;
  Timer? _pollTimer;

  @override
  void dispose() {
    _tokenController.dispose();
    _pollTimer?.cancel();
    super.dispose();
  }

  Future<void> _loginWithToken() async {
    final token = _tokenController.text.trim();
    if (token.isEmpty) {
      _showError('Please enter a token');
      return;
    }

    final authProvider = context.read<AuthProvider>();
    await authProvider.loginWithToken(token);

    if (authProvider.error != null) {
      _showError(authProvider.error!);
    } else {
      context.go('/');
    }
  }

  Future<void> _startDeviceFlow() async {
    setState(() {
      _isDeviceFlowActive = true;
    });

    try {
      final authProvider = context.read<AuthProvider>();
      final result = await authProvider.initiateDeviceFlow();
      
      setState(() {
        _deviceCode = result['device_code'];
        _userCode = result['user_code'];
        _verificationUri = result['verification_uri'];
      });

      // Copy code to clipboard
      await Clipboard.setData(ClipboardData(text: _userCode!));
      
      // Start polling
      _startPolling();
      
      // Open verification URL
      final uri = Uri.parse(_verificationUri!);
      if (await canLaunchUrl(uri)) {
        await launchUrl(uri, mode: LaunchMode.externalApplication);
      }
    } catch (e) {
      setState(() {
        _isDeviceFlowActive = false;
      });
      _showError(e.toString());
    }
  }

  void _startPolling() {
    _pollTimer = Timer.periodic(const Duration(seconds: 5), (timer) async {
      if (_deviceCode == null) return;

      final authProvider = context.read<AuthProvider>();
      final success = await authProvider.pollForDeviceToken(_deviceCode!);
      
      if (success) {
        timer.cancel();
        context.go('/');
      } else if (authProvider.error != null && 
                 !authProvider.error!.contains('authorization_pending')) {
        timer.cancel();
        setState(() {
          _isDeviceFlowActive = false;
        });
        _showError(authProvider.error!);
      }
    });
  }

  void _showError(String message) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        backgroundColor: AppTheme.error,
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final authProvider = context.watch<AuthProvider>();

    return Scaffold(
      backgroundColor: AppTheme.background,
      body: SafeArea(
        child: Padding(
          padding: const EdgeInsets.all(24.0),
          child: SingleChildScrollView(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
              const Icon(
                Icons.code,
                size: 80,
                color: AppTheme.primary,
              ),
              const SizedBox(height: 24),
              Text(
                'GitHub Issue Manager',
                style: Theme.of(context).textTheme.headlineMedium?.copyWith(
                  fontWeight: FontWeight.bold,
                  color: AppTheme.onBackground,
                ),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 8),
              Text(
                'Sign in to manage your GitHub issues',
                style: Theme.of(context).textTheme.bodyLarge?.copyWith(
                  color: AppTheme.secondary,
                ),
                textAlign: TextAlign.center,
              ),
              const SizedBox(height: 48),
              
              // Personal Access Token Method
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(16.0),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          const Icon(Icons.key, color: AppTheme.primary),
                          const SizedBox(width: 8),
                          Text(
                            'Personal Access Token',
                            style: Theme.of(context).textTheme.titleMedium?.copyWith(
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                          const SizedBox(width: 8),
                          Container(
                            padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                            decoration: BoxDecoration(
                              color: AppTheme.success.withValues(alpha: 0.2),
                              borderRadius: BorderRadius.circular(12),
                            ),
                            child: const Text(
                              'Recommended',
                              style: TextStyle(
                                fontSize: 11,
                                color: AppTheme.success,
                                fontWeight: FontWeight.bold,
                              ),
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 12),
                      TextField(
                        controller: _tokenController,
                        obscureText: !_isTokenVisible,
                        decoration: InputDecoration(
                          hintText: 'ghp_xxxxxxxxxxxxxxxxxxxx',
                          suffixIcon: IconButton(
                            icon: Icon(
                              _isTokenVisible ? Icons.visibility_off : Icons.visibility,
                              color: AppTheme.secondary,
                            ),
                            onPressed: () {
                              setState(() {
                                _isTokenVisible = !_isTokenVisible;
                              });
                            },
                          ),
                        ),
                      ),
                      const SizedBox(height: 12),
                      SizedBox(
                        width: double.infinity,
                        child: ElevatedButton(
                          onPressed: authProvider.isLoading ? null : _loginWithToken,
                          child: authProvider.isLoading
                              ? const SizedBox(
                                  width: 20,
                                  height: 20,
                                  child: CircularProgressIndicator(strokeWidth: 2),
                                )
                              : const Text('Sign In with Token'),
                        ),
                      ),
                      const SizedBox(height: 8),
                      TextButton(
                        onPressed: () async {
                          final uri = Uri.parse('https://github.com/settings/tokens/new?description=GitHub%20Issue%20Manager&scopes=repo');
                          if (await canLaunchUrl(uri)) {
                            await launchUrl(uri, mode: LaunchMode.externalApplication);
                          }
                        },
                        child: const Text('Create a new token'),
                      ),
                    ],
                  ),
                ),
              ),
              
              const SizedBox(height: 16),
              
              // Device Flow Method
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(16.0),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          const Icon(Icons.devices, color: AppTheme.primary),
                          const SizedBox(width: 8),
                          Text(
                            'Device Flow',
                            style: Theme.of(context).textTheme.titleMedium?.copyWith(
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 12),
                      if (_isDeviceFlowActive && _userCode != null) ...[
                        Container(
                          padding: const EdgeInsets.all(16),
                          decoration: BoxDecoration(
                            color: AppTheme.surface,
                            borderRadius: BorderRadius.circular(8),
                            border: Border.all(color: AppTheme.outline),
                          ),
                          child: Column(
                            children: [
                              const Text(
                                'Enter this code on GitHub:',
                                style: TextStyle(color: AppTheme.secondary),
                              ),
                              const SizedBox(height: 8),
                              Text(
                                _userCode!,
                                style: const TextStyle(
                                  fontSize: 32,
                                  fontWeight: FontWeight.bold,
                                  letterSpacing: 4,
                                  color: AppTheme.primary,
                                ),
                              ),
                              const SizedBox(height: 8),
                              const Text(
                                'Code copied to clipboard',
                                style: TextStyle(
                                  fontSize: 12,
                                  color: AppTheme.success,
                                ),
                              ),
                              const SizedBox(height: 16),
                              const CircularProgressIndicator(),
                              const SizedBox(height: 8),
                              const Text(
                                'Waiting for authorization...',
                                style: TextStyle(color: AppTheme.secondary),
                              ),
                            ],
                          ),
                        ),
                      ] else ...[
                        Text(
                          'Sign in using a code on another device',
                          style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                            color: AppTheme.secondary,
                          ),
                        ),
                        const SizedBox(height: 12),
                        SizedBox(
                          width: double.infinity,
                          child: ElevatedButton(
                            onPressed: authProvider.isLoading || _isDeviceFlowActive
                                ? null
                                : _startDeviceFlow,
                            style: ElevatedButton.styleFrom(
                              backgroundColor: AppTheme.surface,
                              foregroundColor: AppTheme.primary,
                            ),
                            child: const Text('Start Device Flow'),
                          ),
                        ),
                      ],
                    ],
                  ),
                ),
              ),
            ],
          ),
          ),
        ),
      ),
    );
  }
}