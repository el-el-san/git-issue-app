import 'dart:convert';
import 'package:flutter/foundation.dart';
import 'package:git_issue_app/src/models/user.dart';
import 'package:git_issue_app/src/services/github_api_service.dart';
import 'package:git_issue_app/src/services/secure_storage_service.dart';

class AuthProvider extends ChangeNotifier {
  final GitHubApiService _apiService = GitHubApiService();
  
  User? _user;
  bool _isLoading = true;
  String? _error;

  User? get user => _user;
  bool get isAuthenticated => _user != null;
  bool get isLoading => _isLoading;
  String? get error => _error;

  AuthProvider() {
    _initialize();
  }

  Future<void> _initialize() async {
    await SecureStorageService.init();
    await checkAuthStatus();
  }

  Future<void> checkAuthStatus() async {
    _isLoading = true;
    notifyListeners();

    try {
      final token = await SecureStorageService.getToken();
      if (token != null) {
        final userData = await _apiService.validateToken(token);
        _user = User.fromJson(userData);
        await SecureStorageService.saveUserData(jsonEncode(userData));
      } else {
        // Try to load cached user data
        final cachedData = await SecureStorageService.getUserData();
        if (cachedData != null) {
          _user = User.fromJson(jsonDecode(cachedData));
        }
      }
    } catch (e) {
      _error = e.toString();
      // If validation fails, try cached data
      final cachedData = await SecureStorageService.getUserData();
      if (cachedData != null) {
        _user = User.fromJson(jsonDecode(cachedData));
        _error = null; // Clear error if we have cached data
      }
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<void> loginWithToken(String token) async {
    _isLoading = true;
    _error = null;
    notifyListeners();

    try {
      final userData = await _apiService.validateToken(token);
      _user = User.fromJson(userData);
      
      await SecureStorageService.saveToken(token);
      await SecureStorageService.saveUserData(jsonEncode(userData));
      
      _error = null;
    } catch (e) {
      _error = e.toString();
      _user = null;
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<Map<String, dynamic>> initiateDeviceFlow() async {
    try {
      return await _apiService.initiateDeviceFlow();
    } catch (e) {
      _error = e.toString();
      notifyListeners();
      rethrow;
    }
  }

  Future<bool> pollForDeviceToken(String deviceCode) async {
    try {
      final result = await _apiService.pollForToken(deviceCode);
      if (result != null && result['access_token'] != null) {
        await loginWithToken(result['access_token']);
        return true;
      }
      return false;
    } catch (e) {
      if (!e.toString().contains('authorization_pending')) {
        _error = e.toString();
        notifyListeners();
      }
      return false;
    }
  }

  Future<void> logout() async {
    _isLoading = true;
    notifyListeners();

    await SecureStorageService.clearAll();
    _user = null;
    _error = null;
    
    _isLoading = false;
    notifyListeners();
  }

  void clearError() {
    _error = null;
    notifyListeners();
  }
}