import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:provider/provider.dart';
import 'package:git_issue_app/src/app.dart';
import 'package:git_issue_app/src/providers/auth_provider.dart';
import 'package:git_issue_app/src/providers/repository_provider.dart';
import 'package:git_issue_app/src/models/user.dart';

// Mock AuthProvider for testing
class MockAuthProvider extends AuthProvider {
  bool _isAuthenticated;
  bool _isLoading;
  User? _mockUser;
  String? _mockError;

  MockAuthProvider({
    bool isAuthenticated = false,
    bool isLoading = false,
    User? user,
    String? error,
  }) : _isAuthenticated = isAuthenticated,
       _isLoading = isLoading,
       _mockUser = user,
       _mockError = error;

  @override
  User? get user => _mockUser;

  @override
  bool get isAuthenticated => _isAuthenticated;

  @override
  bool get isLoading => _isLoading;

  @override
  String? get error => _mockError;

  @override
  Future<void> checkAuthStatus() async {
    // Do nothing in mock
  }

  @override
  Future<void> loginWithToken(String token) async {
    // Mock implementation
    _isLoading = true;
    notifyListeners();
    
    await Future.delayed(const Duration(milliseconds: 100));
    
    _isAuthenticated = true;
    _mockUser = User(
      login: 'testuser',
      avatarUrl: 'https://github.com/testuser.png',
      name: 'Test User',
      bio: 'Test bio',
      publicRepos: 10,
      followers: 5,
      following: 3,
    );
    _isLoading = false;
    notifyListeners();
  }

  @override
  Future<void> logout() async {
    _isAuthenticated = false;
    _mockUser = null;
    _mockError = null;
    notifyListeners();
  }
}

void main() {
  group('App Widget Tests', () {
    testWidgets('App starts and shows login screen when not authenticated', (WidgetTester tester) async {
      await tester.pumpWidget(
        MultiProvider(
          providers: [
            ChangeNotifierProvider<AuthProvider>(
              create: (_) => MockAuthProvider(isAuthenticated: false)
            ),
            ChangeNotifierProvider(create: (_) => RepositoryProvider()),
          ],
          child: const GitIssueApp(),
        ),
      );

      // Initial pump to render the widget
      await tester.pump();
      
      // Allow some time for navigation
      await tester.pump(const Duration(milliseconds: 100));

      // Should show login screen when not authenticated
      expect(find.text('GitHub Issue Manager'), findsOneWidget);
      expect(find.text('Sign in to manage your GitHub issues'), findsOneWidget);
    });

    testWidgets('Login screen has required elements', (WidgetTester tester) async {
      await tester.pumpWidget(
        MultiProvider(
          providers: [
            ChangeNotifierProvider<AuthProvider>(
              create: (_) => MockAuthProvider(isAuthenticated: false)
            ),
            ChangeNotifierProvider(create: (_) => RepositoryProvider()),
          ],
          child: const GitIssueApp(),
        ),
      );

      // Initial pump and wait for navigation
      await tester.pump();
      await tester.pump(const Duration(milliseconds: 100));

      // Check for login methods
      expect(find.text('Personal Access Token'), findsOneWidget);
      expect(find.text('Device Flow'), findsOneWidget);
      expect(find.text('Sign In with Token'), findsOneWidget);
      expect(find.text('Start Device Flow'), findsOneWidget);
    });
  });

  group('Theme Tests', () {
    testWidgets('App uses dark theme', (WidgetTester tester) async {
      await tester.pumpWidget(
        MultiProvider(
          providers: [
            ChangeNotifierProvider<AuthProvider>(
              create: (_) => MockAuthProvider(isAuthenticated: false)
            ),
            ChangeNotifierProvider(create: (_) => RepositoryProvider()),
          ],
          child: const GitIssueApp(),
        ),
      );

      // Initial pump to render the widget
      await tester.pump();

      final MaterialApp app = tester.widget(find.byType(MaterialApp).first);
      expect(app.theme?.brightness, Brightness.dark);
    });
  });
}