import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:git_issue_app/src/providers/repository_provider.dart';
import 'package:git_issue_app/src/services/github_api_service.dart';
import 'package:git_issue_app/src/services/secure_storage_service.dart';
import 'package:git_issue_app/src/theme/app_theme.dart';

class CreateIssueScreen extends StatefulWidget {
  const CreateIssueScreen({super.key});

  @override
  State<CreateIssueScreen> createState() => _CreateIssueScreenState();
}

class _CreateIssueScreenState extends State<CreateIssueScreen> {
  final GitHubApiService _apiService = GitHubApiService();
  final _titleController = TextEditingController();
  final _bodyController = TextEditingController();
  final _customTextController = TextEditingController();
  final _formKey = GlobalKey<FormState>();
  
  bool _isSubmitting = false;
  List<String> _selectedLabels = [];
  List<Map<String, dynamic>> _availableLabels = [];
  bool _isLoadingLabels = false;
  bool _includeCustomText = false;
  bool _includeDateInTitle = false;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _loadLabels();
      _loadPreferences();
    });
  }

  Future<void> _loadPreferences() async {
    final customText = await SecureStorageService.getCustomText();
    final includeCustomText = await SecureStorageService.getIncludeCustomText();
    final includeDateInTitle = await SecureStorageService.getIncludeDateInTitle();
    
    setState(() {
      _customTextController.text = customText ?? '';
      _includeCustomText = includeCustomText;
      _includeDateInTitle = includeDateInTitle;
    });
  }

  @override
  void dispose() {
    _titleController.dispose();
    _bodyController.dispose();
    _customTextController.dispose();
    super.dispose();
  }

  Future<void> _loadLabels() async {
    final repository = context.read<RepositoryProvider>();
    if (!repository.isSet) return;

    setState(() {
      _isLoadingLabels = true;
    });

    try {
      final labels = await _apiService.getLabels(
        repository.owner!,
        repository.name!,
      );
      setState(() {
        _availableLabels = List<Map<String, dynamic>>.from(labels);
      });
    } catch (e) {
      // Ignore label loading errors
    } finally {
      setState(() {
        _isLoadingLabels = false;
      });
    }
  }

  Future<void> _createIssue() async {
    if (!_formKey.currentState!.validate()) return;

    final repository = context.read<RepositoryProvider>();
    if (!repository.isSet) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Please set a repository first'),
          backgroundColor: AppTheme.error,
        ),
      );
      return;
    }

    setState(() {
      _isSubmitting = true;
    });

    try {
      String finalTitle = _titleController.text.trim();
      String finalBody = _bodyController.text.trim();
      
      // Add date to title if enabled
      if (_includeDateInTitle) {
        final now = DateTime.now();
        final dateStr = '${now.year}/${now.month.toString().padLeft(2, '0')}/${now.day.toString().padLeft(2, '0')} ${now.hour.toString().padLeft(2, '0')}:${now.minute.toString().padLeft(2, '0')}';
        finalTitle = '[$dateStr] $finalTitle';
      }
      
      // Add custom text to body if enabled
      if (_includeCustomText && _customTextController.text.isNotEmpty) {
        if (finalBody.isNotEmpty) {
          finalBody = '$finalBody\n\n${_customTextController.text.trim()}';
        } else {
          finalBody = _customTextController.text.trim();
        }
      }
      
      final issue = await _apiService.createIssue(
        repository.owner!,
        repository.name!,
        title: finalTitle,
        body: finalBody,
        labels: _selectedLabels,
      );

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Issue created successfully'),
            backgroundColor: AppTheme.success,
          ),
        );
        
        // Clear form
        _titleController.clear();
        _bodyController.clear();
        setState(() {
          _selectedLabels = [];
        });
        
        // Navigate to the new issue
        context.push('/issue/${issue['number']}');
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Failed to create issue: $e'),
            backgroundColor: AppTheme.error,
          ),
        );
      }
    } finally {
      setState(() {
        _isSubmitting = false;
      });
    }
  }

  Widget _buildLabelSelector() {
    if (_availableLabels.isEmpty) return const SizedBox.shrink();

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        const SizedBox(height: 16),
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            const Text(
              'Labels',
              style: TextStyle(
                fontSize: 16,
                fontWeight: FontWeight.w500,
              ),
            ),
            if (_isLoadingLabels)
              const SizedBox(
                width: 16,
                height: 16,
                child: CircularProgressIndicator(strokeWidth: 2),
              ),
          ],
        ),
        const SizedBox(height: 8),
        Wrap(
          spacing: 8,
          runSpacing: 8,
          children: _availableLabels.map((label) {
            final isSelected = _selectedLabels.contains(label['name']);
            return FilterChip(
              label: Text(label['name']),
              selected: isSelected,
              backgroundColor: Color(int.parse('0xFF${label['color']}')).withValues(alpha: 0.2),
              selectedColor: Color(int.parse('0xFF${label['color']}')).withValues(alpha: 0.4),
              labelStyle: TextStyle(
                color: isSelected ? Colors.white : AppTheme.onSurface,
                fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
              ),
              onSelected: (selected) {
                setState(() {
                  if (selected) {
                    _selectedLabels.add(label['name']);
                  } else {
                    _selectedLabels.remove(label['name']);
                  }
                });
              },
            );
          }).toList(),
        ),
      ],
    );
  }

  @override
  Widget build(BuildContext context) {
    final repository = context.watch<RepositoryProvider>();

    return Scaffold(
      appBar: AppBar(
        title: const Text('Create Issue'),
      ),
      body: !repository.isSet
          ? Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(
                    Icons.folder_open,
                    size: 64,
                    color: AppTheme.secondary,
                  ),
                  const SizedBox(height: 16),
                  Text(
                    'No repository selected',
                    style: TextStyle(
                      fontSize: 18,
                      color: AppTheme.secondary,
                    ),
                  ),
                  const SizedBox(height: 8),
                  Text(
                    'Please select a repository to create issues',
                    style: TextStyle(
                      color: AppTheme.secondary,
                    ),
                  ),
                  const SizedBox(height: 16),
                  ElevatedButton(
                    onPressed: () {
                      context.push('/repository-settings');
                    },
                    child: const Text('Set Repository'),
                  ),
                ],
              ),
            )
          : SingleChildScrollView(
              padding: const EdgeInsets.all(16),
              child: Form(
                key: _formKey,
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    Card(
                      child: Padding(
                        padding: const EdgeInsets.all(16),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Row(
                              children: [
                                Icon(Icons.folder, color: AppTheme.primary, size: 20),
                                const SizedBox(width: 8),
                                Text(
                                  repository.fullName!,
                                  style: TextStyle(
                                    color: AppTheme.primary,
                                    fontWeight: FontWeight.w500,
                                  ),
                                ),
                              ],
                            ),
                          ],
                        ),
                      ),
                    ),
                    const SizedBox(height: 16),
                    TextFormField(
                      controller: _titleController,
                      decoration: const InputDecoration(
                        labelText: 'Title',
                        hintText: 'Issue title',
                        border: OutlineInputBorder(),
                      ),
                      validator: (value) {
                        if (value == null || value.trim().isEmpty) {
                          return 'Title is required';
                        }
                        return null;
                      },
                      textInputAction: TextInputAction.next,
                    ),
                    const SizedBox(height: 16),
                    TextFormField(
                      controller: _bodyController,
                      decoration: const InputDecoration(
                        labelText: 'Description',
                        hintText: 'Leave a comment',
                        border: OutlineInputBorder(),
                        alignLabelWithHint: true,
                      ),
                      maxLines: 8,
                      keyboardType: TextInputType.multiline,
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
                                Icon(Icons.settings, color: AppTheme.primary, size: 20),
                                const SizedBox(width: 8),
                                const Text(
                                  'Issue Options',
                                  style: TextStyle(fontWeight: FontWeight.bold),
                                ),
                              ],
                            ),
                            const SizedBox(height: 16),
                            SwitchListTile(
                              title: const Text('Include date in title'),
                              subtitle: const Text('Adds current date/time to issue title'),
                              value: _includeDateInTitle,
                              onChanged: (value) async {
                                setState(() {
                                  _includeDateInTitle = value;
                                });
                                await SecureStorageService.saveIncludeDateInTitle(value);
                              },
                            ),
                            const SizedBox(height: 8),
                            SwitchListTile(
                              title: const Text('Include custom text in description'),
                              subtitle: const Text('Adds specified text to issue description'),
                              value: _includeCustomText,
                              onChanged: (value) async {
                                setState(() {
                                  _includeCustomText = value;
                                });
                                await SecureStorageService.saveIncludeCustomText(value);
                              },
                            ),
                            if (_includeCustomText) ...[
                              const SizedBox(height: 16),
                              TextFormField(
                                controller: _customTextController,
                                decoration: const InputDecoration(
                                  labelText: 'Custom text',
                                  hintText: 'Text to append to all issue descriptions',
                                  border: OutlineInputBorder(),
                                ),
                                maxLines: 3,
                                onChanged: (value) async {
                                  await SecureStorageService.saveCustomText(value);
                                },
                              ),
                            ],
                          ],
                        ),
                      ),
                    ),
                    _buildLabelSelector(),
                    const SizedBox(height: 24),
                    ElevatedButton(
                      onPressed: _isSubmitting ? null : _createIssue,
                      style: ElevatedButton.styleFrom(
                        padding: const EdgeInsets.symmetric(vertical: 16),
                      ),
                      child: _isSubmitting
                          ? const SizedBox(
                              width: 20,
                              height: 20,
                              child: CircularProgressIndicator(
                                strokeWidth: 2,
                                color: Colors.white,
                              ),
                            )
                          : const Text(
                              'Submit new issue',
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
                                Icon(Icons.info_outline, color: AppTheme.primary, size: 20),
                                const SizedBox(width: 8),
                                const Text(
                                  'Markdown Support',
                                  style: TextStyle(fontWeight: FontWeight.bold),
                                ),
                              ],
                            ),
                            const SizedBox(height: 8),
                            Text(
                              'You can use Markdown formatting in the description:\n'
                              '• **bold** for bold text\n'
                              '• *italic* for italic text\n'
                              '• `code` for inline code\n'
                              '• ```language for code blocks\n'
                              '• - or * for bullet lists\n'
                              '• [text](url) for links',
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