import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:git_issue_app/src/app.dart';
import 'package:git_issue_app/src/config/app_config.dart';
import 'package:git_issue_app/src/providers/auth_provider.dart';
import 'package:git_issue_app/src/providers/repository_provider.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  
  // Load configuration
  await AppConfig.load();
  
  runApp(
    MultiProvider(
      providers: [
        ChangeNotifierProvider(create: (_) => AuthProvider()),
        ChangeNotifierProvider(create: (_) => RepositoryProvider()),
      ],
      child: const GitIssueApp(),
    ),
  );
}