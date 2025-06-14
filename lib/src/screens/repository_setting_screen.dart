import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:git_issue_app/src/providers/repository_provider.dart';
import 'package:git_issue_app/src/theme/app_theme.dart';

class RepositorySettingScreen extends StatefulWidget {
  const RepositorySettingScreen({super.key});

  @override
  State<RepositorySettingScreen> createState() => _RepositorySettingScreenState();
}

class _RepositorySettingScreenState extends State<RepositorySettingScreen> {
  final _formKey = GlobalKey<FormState>();
  final _ownerController = TextEditingController();
  final _nameController = TextEditingController();
  final _combinedController = TextEditingController();
  
  bool _isAdvancedMode = false;
  bool _isSaving = false;

  @override
  void initState() {
    super.initState();
    final repository = context.read<RepositoryProvider>();
    if (repository.isSet) {
      _ownerController.text = repository.owner ?? '';
      _nameController.text = repository.name ?? '';
      _combinedController.text = repository.fullName ?? '';
    }
  }

  @override
  void dispose() {
    _ownerController.dispose();
    _nameController.dispose();
    _combinedController.dispose();
    super.dispose();
  }

  Future<void> _saveRepository() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() {
      _isSaving = true;
    });

    try {
      final repository = context.read<RepositoryProvider>();
      
      if (_isAdvancedMode) {
        await repository.setRepository(
          _ownerController.text.trim(),
          _nameController.text.trim(),
        );
      } else {
        final parts = _combinedController.text.trim().split('/');
        if (parts.length == 2) {
          await repository.setRepository(parts[0], parts[1]);
        }
      }

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Repository saved successfully'),
            backgroundColor: AppTheme.success,
          ),
        );
        context.pop();
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Failed to save repository: $e'),
            backgroundColor: AppTheme.error,
          ),
        );
      }
    } finally {
      setState(() {
        _isSaving = false;
      });
    }
  }

  Widget _buildSimpleMode() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text(
          'Enter repository in owner/name format',
          style: TextStyle(fontSize: 14, color: AppTheme.secondary),
        ),
        const SizedBox(height: 16),
        TextFormField(
          controller: _combinedController,
          decoration: const InputDecoration(
            labelText: 'Repository',
            hintText: 'owner/repository',
            prefixIcon: Icon(Icons.folder),
          ),
          validator: (value) {
            if (value == null || value.trim().isEmpty) {
              return 'Repository is required';
            }
            final parts = value.trim().split('/');
            if (parts.length != 2 || parts[0].isEmpty || parts[1].isEmpty) {
              return 'Invalid format. Use owner/repository';
            }
            return null;
          },
        ),
      ],
    );
  }

  Widget _buildAdvancedMode() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const Text(
          'Enter owner and repository name separately',
          style: TextStyle(fontSize: 14, color: AppTheme.secondary),
        ),
        const SizedBox(height: 16),
        TextFormField(
          controller: _ownerController,
          decoration: const InputDecoration(
            labelText: 'Owner',
            hintText: 'Repository owner or organization',
            prefixIcon: Icon(Icons.person),
          ),
          validator: (value) {
            if (value == null || value.trim().isEmpty) {
              return 'Owner is required';
            }
            return null;
          },
        ),
        const SizedBox(height: 16),
        TextFormField(
          controller: _nameController,
          decoration: const InputDecoration(
            labelText: 'Repository Name',
            hintText: 'Repository name',
            prefixIcon: Icon(Icons.folder),
          ),
          validator: (value) {
            if (value == null || value.trim().isEmpty) {
              return 'Repository name is required';
            }
            return null;
          },
        ),
      ],
    );
  }

  @override
  Widget build(BuildContext context) {
    final repository = context.watch<RepositoryProvider>();

    return Scaffold(
      appBar: AppBar(
        title: const Text('Repository Settings'),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Form(
          key: _formKey,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              if (repository.isSet) ...[
                Card(
                  child: Padding(
                    padding: const EdgeInsets.all(16),
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Row(
                          children: [
                            Icon(Icons.folder, color: AppTheme.primary),
                            const SizedBox(width: 8),
                            const Text(
                              'Current Repository',
                              style: TextStyle(
                                fontWeight: FontWeight.bold,
                                fontSize: 16,
                              ),
                            ),
                          ],
                        ),
                        const SizedBox(height: 8),
                        Text(
                          repository.fullName!,
                          style: TextStyle(
                            color: AppTheme.primary,
                            fontSize: 18,
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
                const SizedBox(height: 24),
              ],
              
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(16),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          const Text(
                            'Repository Configuration',
                            style: TextStyle(
                              fontWeight: FontWeight.bold,
                              fontSize: 16,
                            ),
                          ),
                          TextButton(
                            onPressed: () {
                              setState(() {
                                _isAdvancedMode = !_isAdvancedMode;
                                if (_isAdvancedMode) {
                                  // Parse combined input to separate fields
                                  final parts = _combinedController.text.split('/');
                                  if (parts.length == 2) {
                                    _ownerController.text = parts[0];
                                    _nameController.text = parts[1];
                                  }
                                } else {
                                  // Combine separate fields
                                  if (_ownerController.text.isNotEmpty && 
                                      _nameController.text.isNotEmpty) {
                                    _combinedController.text = 
                                      '${_ownerController.text}/${_nameController.text}';
                                  }
                                }
                              });
                            },
                            child: Text(_isAdvancedMode ? 'Simple Mode' : 'Advanced Mode'),
                          ),
                        ],
                      ),
                      const SizedBox(height: 16),
                      AnimatedSwitcher(
                        duration: const Duration(milliseconds: 300),
                        child: _isAdvancedMode 
                            ? _buildAdvancedMode() 
                            : _buildSimpleMode(),
                      ),
                    ],
                  ),
                ),
              ),
              
              const SizedBox(height: 24),
              
              ElevatedButton(
                onPressed: _isSaving ? null : _saveRepository,
                style: ElevatedButton.styleFrom(
                  padding: const EdgeInsets.symmetric(vertical: 16),
                ),
                child: _isSaving
                    ? const SizedBox(
                        width: 20,
                        height: 20,
                        child: CircularProgressIndicator(
                          strokeWidth: 2,
                          color: Colors.white,
                        ),
                      )
                    : const Text(
                        'Save Repository',
                        style: TextStyle(fontSize: 16),
                      ),
              ),
              
              const SizedBox(height: 16),
              
              Card(
                child: Padding(
                  padding: const EdgeInsets.all(16),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          Icon(Icons.info_outline, color: AppTheme.warning),
                          const SizedBox(width: 8),
                          const Text(
                            'Important Notes',
                            style: TextStyle(
                              fontWeight: FontWeight.bold,
                              fontSize: 14,
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 8),
                      Text(
                        '• You must have access to the repository\n'
                        '• Private repositories require appropriate permissions\n'
                        '• Organization repositories may have restrictions\n'
                        '• Repository names are case-sensitive',
                        style: TextStyle(
                          fontSize: 13,
                          color: AppTheme.secondary,
                        ),
                      ),
                    ],
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}