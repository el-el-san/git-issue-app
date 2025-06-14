import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:git_issue_app/src/providers/auth_provider.dart';
import 'package:git_issue_app/src/screens/login_screen.dart';
import 'package:git_issue_app/src/screens/home_screen.dart';
import 'package:git_issue_app/src/screens/issue_detail_screen.dart';
import 'package:git_issue_app/src/screens/repository_setting_screen.dart';
import 'package:git_issue_app/src/theme/app_theme.dart';

class GitIssueApp extends StatefulWidget {
  const GitIssueApp({super.key});

  @override
  State<GitIssueApp> createState() => _GitIssueAppState();
}

class _GitIssueAppState extends State<GitIssueApp> {
  late final GoRouter _router;

  @override
  void initState() {
    super.initState();
    _router = GoRouter(
      initialLocation: '/',
      refreshListenable: context.read<AuthProvider>(),
      redirect: (context, state) {
        final authProvider = context.read<AuthProvider>();
        final isLoading = authProvider.isLoading;
        final isAuthenticated = authProvider.isAuthenticated;
        final isAuthRoute = state.matchedLocation == '/login';

        // Wait for authentication check to complete
        if (isLoading) {
          return null; // Keep current route while loading
        }

        if (!isAuthenticated && !isAuthRoute) {
          return '/login';
        }
        if (isAuthenticated && isAuthRoute) {
          return '/';
        }
        return null;
      },
      routes: [
        GoRoute(
          path: '/login',
          builder: (context, state) => const LoginScreen(),
        ),
        GoRoute(
          path: '/',
          builder: (context, state) => const HomeScreen(),
          routes: [
            GoRoute(
              path: 'issue/:issueNumber',
              builder: (context, state) {
                final issueNumber = int.parse(state.pathParameters['issueNumber']!);
                return IssueDetailScreen(issueNumber: issueNumber);
              },
            ),
            GoRoute(
              path: 'repository-settings',
              builder: (context, state) => const RepositorySettingScreen(),
            ),
          ],
        ),
      ],
    );
  }

  @override
  Widget build(BuildContext context) {
    return Consumer<AuthProvider>(
      builder: (context, authProvider, child) {
        // Show loading screen while initializing
        if (authProvider.isLoading) {
          return MaterialApp(
            title: 'GitHub Issue Manager',
            theme: AppTheme.darkTheme,
            debugShowCheckedModeBanner: false,
            home: const Scaffold(
              body: Center(
                child: CircularProgressIndicator(),
              ),
            ),
          );
        }
        
        return MaterialApp.router(
          title: 'GitHub Issue Manager',
          theme: AppTheme.darkTheme,
          routerConfig: _router,
          debugShowCheckedModeBanner: false,
        );
      },
    );
  }
}