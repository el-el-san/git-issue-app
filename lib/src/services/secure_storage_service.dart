import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import 'package:shared_preferences/shared_preferences.dart';

class SecureStorageService {
  static const _storage = FlutterSecureStorage();
  static SharedPreferences? _prefs;

  static const String _tokenKey = 'github_token';
  static const String _userKey = 'github_user';
  static const String _repoKey = 'selected_repository';
  static const String _customTextKey = 'issue_custom_text';
  static const String _includeCustomTextKey = 'issue_include_custom_text';
  static const String _includeDateInTitleKey = 'issue_include_date_in_title';

  static Future<void> init() async {
    _prefs = await SharedPreferences.getInstance();
  }

  // Token management
  static Future<void> saveToken(String token) async {
    try {
      await _storage.write(key: _tokenKey, value: token);
    } catch (e) {
      // Fallback to SharedPreferences if secure storage fails
      await _prefs?.setString(_tokenKey, token);
    }
  }

  static Future<String?> getToken() async {
    try {
      final token = await _storage.read(key: _tokenKey);
      if (token != null) return token;
    } catch (e) {
      // Try fallback
    }
    return _prefs?.getString(_tokenKey);
  }

  static Future<void> deleteToken() async {
    try {
      await _storage.delete(key: _tokenKey);
    } catch (e) {
      // Continue with fallback
    }
    await _prefs?.remove(_tokenKey);
  }

  // User data management
  static Future<void> saveUserData(String userData) async {
    await _prefs?.setString(_userKey, userData);
  }

  static Future<String?> getUserData() async {
    return _prefs?.getString(_userKey);
  }

  static Future<void> deleteUserData() async {
    await _prefs?.remove(_userKey);
  }

  // Repository management
  static Future<void> saveRepository(String repository) async {
    await _prefs?.setString(_repoKey, repository);
  }

  static Future<String?> getRepository() async {
    return _prefs?.getString(_repoKey);
  }

  static Future<void> deleteRepository() async {
    await _prefs?.remove(_repoKey);
  }

  // Issue creation preferences
  static Future<void> saveCustomText(String text) async {
    await _prefs?.setString(_customTextKey, text);
  }

  static Future<String?> getCustomText() async {
    return _prefs?.getString(_customTextKey);
  }

  static Future<void> saveIncludeCustomText(bool value) async {
    await _prefs?.setBool(_includeCustomTextKey, value);
  }

  static Future<bool> getIncludeCustomText() async {
    return _prefs?.getBool(_includeCustomTextKey) ?? false;
  }

  static Future<void> saveIncludeDateInTitle(bool value) async {
    await _prefs?.setBool(_includeDateInTitleKey, value);
  }

  static Future<bool> getIncludeDateInTitle() async {
    return _prefs?.getBool(_includeDateInTitleKey) ?? false;
  }

  // Clear all data
  static Future<void> clearAll() async {
    await deleteToken();
    await deleteUserData();
    await deleteRepository();
  }
}