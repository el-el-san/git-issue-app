import 'package:flutter/foundation.dart';
import 'package:git_issue_app/src/services/secure_storage_service.dart';

class RepositoryProvider extends ChangeNotifier {
  String? _owner;
  String? _name;
  bool _isLoading = false;

  String? get owner => _owner;
  String? get name => _name;
  String? get fullName => (_owner != null && _name != null) ? '$_owner/$_name' : null;
  bool get isSet => _owner != null && _name != null;
  bool get isLoading => _isLoading;

  RepositoryProvider() {
    _loadRepository();
  }

  Future<void> _loadRepository() async {
    _isLoading = true;
    notifyListeners();

    try {
      final savedRepo = await SecureStorageService.getRepository();
      if (savedRepo != null && savedRepo.contains('/')) {
        final parts = savedRepo.split('/');
        _owner = parts[0];
        _name = parts[1];
      }
    } catch (e) {
      // Ignore errors when loading
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<void> setRepository(String owner, String name) async {
    _isLoading = true;
    notifyListeners();

    try {
      _owner = owner;
      _name = name;
      await SecureStorageService.saveRepository('$owner/$name');
    } catch (e) {
      // Continue even if save fails
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }

  Future<void> clearRepository() async {
    _isLoading = true;
    notifyListeners();

    try {
      _owner = null;
      _name = null;
      await SecureStorageService.deleteRepository();
    } catch (e) {
      // Continue even if delete fails
    } finally {
      _isLoading = false;
      notifyListeners();
    }
  }
}