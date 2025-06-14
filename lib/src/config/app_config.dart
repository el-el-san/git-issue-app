import 'dart:convert';
import 'package:flutter/services.dart';

class AppConfig {
  static const String _configFile = 'assets/config.json';
  static Map<String, dynamic>? _config;

  // Environment variables with fallback
  static const String _githubClientIdKey = 'GH_CLIENT_ID';
  static const String _githubClientIdDefault = 'YOUR_GITHUB_CLIENT_ID_HERE';

  static Future<void> load() async {
    try {
      // Try to load from config file first
      final configString = await rootBundle.loadString(_configFile);
      _config = json.decode(configString);
    } catch (e) {
      // Config file not found or invalid, use defaults
      _config = {};
    }
  }

  static String get githubClientId {
    // Priority order:
    // 1. Environment variable (for production)
    // 2. Config file (for development)
    // 3. Default value (fallback)
    
    // Check environment variable first
    const envValue = String.fromEnvironment(_githubClientIdKey);
    if (envValue.isNotEmpty) {
      return envValue;
    }

    // Check config file
    if (_config != null && _config!.containsKey('github_client_id')) {
      return _config!['github_client_id'];
    }

    // Return default (should be replaced in production)
    return _githubClientIdDefault;
  }

  static bool get isProduction {
    return const bool.fromEnvironment('dart.vm.product');
  }
}