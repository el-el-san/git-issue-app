import 'package:flutter/material.dart';
import 'package:flutter_markdown/flutter_markdown.dart';
import 'package:intl/intl.dart';
import 'package:provider/provider.dart';
import 'package:url_launcher/url_launcher.dart';
import 'package:git_issue_app/src/models/issue.dart';
import 'package:git_issue_app/src/providers/repository_provider.dart';
import 'package:git_issue_app/src/services/github_api_service.dart';
import 'package:git_issue_app/src/theme/app_theme.dart';

class IssueDetailScreen extends StatefulWidget {
  final int issueNumber;

  const IssueDetailScreen({
    super.key,
    required this.issueNumber,
  });

  @override
  State<IssueDetailScreen> createState() => _IssueDetailScreenState();
}

class _IssueDetailScreenState extends State<IssueDetailScreen> {
  final GitHubApiService _apiService = GitHubApiService();
  final TextEditingController _commentController = TextEditingController();
  
  Issue? _issue;
  List<Comment> _comments = [];
  List<Map<String, dynamic>> _availableLabels = [];
  bool _isLoading = true;
  bool _isSubmittingComment = false;
  bool _isEditingLabels = false;
  bool _isUpdatingLabels = false;
  String? _error;

  @override
  void initState() {
    super.initState();
    _loadIssueDetails();
  }

  @override
  void dispose() {
    _commentController.dispose();
    super.dispose();
  }

  Future<void> _loadIssueDetails() async {
    final repository = context.read<RepositoryProvider>();
    if (!repository.isSet) return;

    setState(() {
      _isLoading = true;
      _error = null;
    });

    try {
      final issueData = await _apiService.getIssue(
        repository.owner!,
        repository.name!,
        widget.issueNumber,
      );
      
      final commentsData = await _apiService.getIssueComments(
        repository.owner!,
        repository.name!,
        widget.issueNumber,
      );

      final labelsData = await _apiService.getLabels(
        repository.owner!,
        repository.name!,
      );

      setState(() {
        _issue = Issue.fromJson(issueData);
        _comments = commentsData.map((json) => Comment.fromJson(json)).toList();
        _availableLabels = List<Map<String, dynamic>>.from(labelsData);
      });
    } catch (e) {
      setState(() {
        _error = e.toString();
      });
    } finally {
      setState(() {
        _isLoading = false;
      });
    }
  }

  Future<void> _submitComment() async {
    final comment = _commentController.text.trim();
    if (comment.isEmpty) return;

    final repository = context.read<RepositoryProvider>();
    if (!repository.isSet) return;

    setState(() {
      _isSubmittingComment = true;
    });

    try {
      final newComment = await _apiService.createComment(
        repository.owner!,
        repository.name!,
        widget.issueNumber,
        comment,
      );

      setState(() {
        _comments.add(Comment.fromJson(newComment));
        _commentController.clear();
      });
      
      // Show success message
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Comment posted successfully'),
            backgroundColor: AppTheme.success,
          ),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Failed to post comment: $e'),
            backgroundColor: AppTheme.error,
          ),
        );
      }
    } finally {
      setState(() {
        _isSubmittingComment = false;
      });
    }
  }

  Widget _buildHeader() {
    if (_issue == null) return const SizedBox.shrink();

    final dateFormat = DateFormat('MMM d, yyyy \'at\' h:mm a');

    return Card(
      margin: const EdgeInsets.all(16),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                Icon(
                  _issue!.state == 'open' ? Icons.error_outline : Icons.check_circle_outline,
                  size: 24,
                  color: _issue!.state == 'open' ? AppTheme.issueOpen : AppTheme.issueClosed,
                ),
                const SizedBox(width: 8),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
                  decoration: BoxDecoration(
                    color: _issue!.state == 'open' 
                        ? AppTheme.issueOpen.withOpacity(0.2)
                        : AppTheme.issueClosed.withOpacity(0.2),
                    borderRadius: BorderRadius.circular(16),
                  ),
                  child: Text(
                    _issue!.state.toUpperCase(),
                    style: TextStyle(
                      color: _issue!.state == 'open' ? AppTheme.issueOpen : AppTheme.issueClosed,
                      fontWeight: FontWeight.bold,
                      fontSize: 12,
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),
            Text(
              _issue!.title,
              style: const TextStyle(
                fontSize: 20,
                fontWeight: FontWeight.bold,
              ),
            ),
            const SizedBox(height: 8),
            Row(
              children: [
                CircleAvatar(
                  radius: 12,
                  backgroundImage: NetworkImage(_issue!.user.avatarUrl),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: Text(
                    '${_issue!.user.login} opened this issue ${dateFormat.format(_issue!.createdAt)}',
                    style: TextStyle(
                      color: AppTheme.secondary,
                      fontSize: 14,
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),
            _buildLabelsSection(),
          ],
        ),
      ),
    );
  }

  Widget _buildBody() {
    if (_issue == null || _issue!.body == null || _issue!.body!.isEmpty) {
      return const SizedBox.shrink();
    }

    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 16),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: MarkdownBody(
          data: _issue!.body!,
          styleSheet: MarkdownStyleSheet(
            p: TextStyle(color: AppTheme.onSurface),
            code: TextStyle(
              backgroundColor: AppTheme.surface,
              color: AppTheme.primary,
            ),
            codeblockDecoration: BoxDecoration(
              color: AppTheme.surface,
              borderRadius: BorderRadius.circular(6),
            ),
          ),
          onTapLink: (text, href, title) {
            if (href != null) {
              launchUrl(Uri.parse(href));
            }
          },
        ),
      ),
    );
  }

  Widget _buildComment(Comment comment) {
    final dateFormat = DateFormat('MMM d, yyyy \'at\' h:mm a');

    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Row(
              children: [
                CircleAvatar(
                  radius: 16,
                  backgroundImage: NetworkImage(comment.user.avatarUrl),
                ),
                const SizedBox(width: 8),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        comment.user.login,
                        style: const TextStyle(
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      Text(
                        dateFormat.format(comment.createdAt),
                        style: TextStyle(
                          color: AppTheme.secondary,
                          fontSize: 12,
                        ),
                      ),
                    ],
                  ),
                ),
              ],
            ),
            const SizedBox(height: 12),
            MarkdownBody(
              data: comment.body,
              styleSheet: MarkdownStyleSheet(
                p: TextStyle(color: AppTheme.onSurface),
                code: TextStyle(
                  backgroundColor: AppTheme.surface,
                  color: AppTheme.primary,
                ),
                codeblockDecoration: BoxDecoration(
                  color: AppTheme.surface,
                  borderRadius: BorderRadius.circular(6),
                ),
              ),
              onTapLink: (text, href, title) {
                if (href != null) {
                  launchUrl(Uri.parse(href));
                }
              },
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildLabelsSection() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            Icon(Icons.label, size: 16, color: AppTheme.secondary),
            const SizedBox(width: 4),
            Text(
              'Labels',
              style: TextStyle(
                fontSize: 14,
                color: AppTheme.secondary,
                fontWeight: FontWeight.w500,
              ),
            ),
            const Spacer(),
            if (!_isEditingLabels)
              TextButton.icon(
                onPressed: () {
                  setState(() {
                    _isEditingLabels = true;
                  });
                },
                icon: const Icon(Icons.edit, size: 16),
                label: const Text('Edit'),
                style: TextButton.styleFrom(
                  padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                ),
              ),
          ],
        ),
        const SizedBox(height: 8),
        if (_isEditingLabels)
          _buildLabelEditor()
        else
          _buildLabelDisplay(),
      ],
    );
  }

  Widget _buildLabelDisplay() {
    if (_issue!.labels.isEmpty) {
      return Container(
        padding: const EdgeInsets.all(12),
        decoration: BoxDecoration(
          color: AppTheme.surface,
          borderRadius: BorderRadius.circular(8),
          border: Border.all(color: AppTheme.outline),
        ),
        child: Row(
          children: [
            Icon(Icons.label_outline, size: 16, color: AppTheme.secondary),
            const SizedBox(width: 8),
            Text(
              'No labels',
              style: TextStyle(
                color: AppTheme.secondary,
                fontSize: 14,
              ),
            ),
          ],
        ),
      );
    }

    return Wrap(
      spacing: 4,
      runSpacing: 4,
      children: _issue!.labels.map((label) {
        return Container(
          padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
          decoration: BoxDecoration(
            color: Color(int.parse('0xFF${label.color}')),
            borderRadius: BorderRadius.circular(16),
          ),
          child: Text(
            label.name,
            style: const TextStyle(
              fontSize: 12,
              color: Colors.white,
              fontWeight: FontWeight.w500,
            ),
          ),
        );
      }).toList(),
    );
  }

  Widget _buildLabelEditor() {
    final currentLabels = _issue!.labels.map((l) => l.name).toList();
    
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: AppTheme.surface,
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: AppTheme.outline),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Select labels for this issue:',
            style: TextStyle(
              fontSize: 14,
              color: AppTheme.secondary,
            ),
          ),
          const SizedBox(height: 8),
          if (_availableLabels.isEmpty)
            Text(
              'No labels available',
              style: TextStyle(
                color: AppTheme.secondary,
                fontSize: 14,
              ),
            )
          else
            Wrap(
              spacing: 8,
              runSpacing: 8,
              children: _availableLabels.map((label) {
                final isSelected = currentLabels.contains(label['name']);
                return FilterChip(
                  label: Text(label['name']),
                  selected: isSelected,
                  backgroundColor: Color(int.parse('0xFF${label['color']}')).withOpacity(0.2),
                  selectedColor: Color(int.parse('0xFF${label['color']}')).withOpacity(0.4),
                  labelStyle: TextStyle(
                    color: isSelected ? Colors.white : AppTheme.onSurface,
                    fontWeight: isSelected ? FontWeight.bold : FontWeight.normal,
                  ),
                  onSelected: (selected) {
                    setState(() {
                      if (selected) {
                        if (!currentLabels.contains(label['name'])) {
                          currentLabels.add(label['name']);
                        }
                      } else {
                        currentLabels.remove(label['name']);
                      }
                    });
                  },
                );
              }).toList(),
            ),
          const SizedBox(height: 12),
          Row(
            mainAxisAlignment: MainAxisAlignment.end,
            children: [
              TextButton(
                onPressed: () {
                  setState(() {
                    _isEditingLabels = false;
                  });
                },
                child: const Text('Cancel'),
              ),
              const SizedBox(width: 8),
              ElevatedButton(
                onPressed: _isUpdatingLabels ? null : () => _updateLabels(currentLabels),
                child: _isUpdatingLabels
                    ? const SizedBox(
                        width: 16,
                        height: 16,
                        child: CircularProgressIndicator(strokeWidth: 2),
                      )
                    : const Text('Save'),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Future<void> _updateLabels(List<String> newLabels) async {
    final repository = context.read<RepositoryProvider>();
    if (!repository.isSet) return;

    setState(() {
      _isUpdatingLabels = true;
    });

    try {
      await _apiService.updateIssueLabels(
        repository.owner!,
        repository.name!,
        widget.issueNumber,
        newLabels,
      );

      // Reload issue to get updated labels
      await _loadIssueDetails();

      setState(() {
        _isEditingLabels = false;
      });

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Labels updated successfully'),
            backgroundColor: AppTheme.success,
          ),
        );
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Failed to update labels: $e'),
            backgroundColor: AppTheme.error,
          ),
        );
      }
    } finally {
      setState(() {
        _isUpdatingLabels = false;
      });
    }
  }

  Widget _buildCommentInput() {
    return Card(
      margin: const EdgeInsets.all(16),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            TextField(
              controller: _commentController,
              maxLines: 4,
              decoration: const InputDecoration(
                hintText: 'Leave a comment',
                border: OutlineInputBorder(),
              ),
            ),
            const SizedBox(height: 12),
            ElevatedButton(
              onPressed: _isSubmittingComment ? null : _submitComment,
              child: _isSubmittingComment
                  ? const SizedBox(
                      width: 20,
                      height: 20,
                      child: CircularProgressIndicator(strokeWidth: 2),
                    )
                  : const Text('Comment'),
            ),
          ],
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('#${widget.issueNumber}'),
      ),
      body: _isLoading
          ? const Center(child: CircularProgressIndicator())
          : _error != null
              ? Center(
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Icon(
                        Icons.error_outline,
                        size: 64,
                        color: AppTheme.error,
                      ),
                      const SizedBox(height: 16),
                      Text(
                        'Failed to load issue',
                        style: TextStyle(
                          fontSize: 18,
                          color: AppTheme.error,
                        ),
                      ),
                      const SizedBox(height: 8),
                      Text(
                        _error!,
                        style: TextStyle(
                          color: AppTheme.secondary,
                        ),
                        textAlign: TextAlign.center,
                      ),
                      const SizedBox(height: 16),
                      ElevatedButton(
                        onPressed: _loadIssueDetails,
                        child: const Text('Retry'),
                      ),
                    ],
                  ),
                )
              : ListView(
                  children: [
                    _buildHeader(),
                    _buildBody(),
                    if (_comments.isNotEmpty) ...[
                      const Padding(
                        padding: EdgeInsets.fromLTRB(16, 16, 16, 8),
                        child: Text(
                          'Comments',
                          style: TextStyle(
                            fontSize: 18,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ),
                      ..._comments.map(_buildComment),
                    ],
                    _buildCommentInput(),
                    const SizedBox(height: 80), // Bottom padding
                  ],
                ),
    );
  }
}