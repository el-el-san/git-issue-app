import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import 'package:provider/provider.dart';
import 'package:intl/intl.dart';
import 'package:git_issue_app/src/models/issue.dart';
import 'package:git_issue_app/src/providers/repository_provider.dart';
import 'package:git_issue_app/src/services/github_api_service.dart';
import 'package:git_issue_app/src/theme/app_theme.dart';

class IssueListScreen extends StatefulWidget {
  const IssueListScreen({super.key});

  @override
  State<IssueListScreen> createState() => _IssueListScreenState();
}

class _IssueListScreenState extends State<IssueListScreen> {
  final GitHubApiService _apiService = GitHubApiService();
  final ScrollController _scrollController = ScrollController();
  
  List<Issue> _issues = [];
  bool _isLoading = false;
  bool _hasMore = true;
  String _filter = 'all';
  int _page = 1;
  String? _error;

  @override
  void initState() {
    super.initState();
    _scrollController.addListener(_onScroll);
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _loadIssues();
    });
  }

  @override
  void dispose() {
    _scrollController.dispose();
    super.dispose();
  }

  void _onScroll() {
    if (_scrollController.position.pixels >= _scrollController.position.maxScrollExtent - 200) {
      if (!_isLoading && _hasMore) {
        _loadMoreIssues();
      }
    }
  }

  Future<void> _loadIssues() async {
    final repository = context.read<RepositoryProvider>();
    if (!repository.isSet) {
      setState(() {
        _error = 'Please set a repository first';
      });
      return;
    }

    setState(() {
      _isLoading = true;
      _error = null;
      _page = 1;
      _hasMore = true;
    });

    try {
      final data = await _apiService.getIssues(
        repository.owner!,
        repository.name!,
        state: _filter,
        page: _page,
      );
      
      setState(() {
        _issues = data.map((json) => Issue.fromJson(json)).toList();
        _hasMore = data.length >= 30;
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

  Future<void> _loadMoreIssues() async {
    final repository = context.read<RepositoryProvider>();
    if (!repository.isSet || !_hasMore) return;

    setState(() {
      _page++;
    });

    try {
      final data = await _apiService.getIssues(
        repository.owner!,
        repository.name!,
        state: _filter,
        page: _page,
      );
      
      setState(() {
        _issues.addAll(data.map((json) => Issue.fromJson(json)));
        _hasMore = data.length >= 30;
      });
    } catch (e) {
      setState(() {
        _page--;
      });
    }
  }

  Widget _buildFilterChips() {
    return SingleChildScrollView(
      scrollDirection: Axis.horizontal,
      padding: const EdgeInsets.symmetric(horizontal: 16),
      child: Row(
        children: [
          FilterChip(
            label: const Text('All'),
            selected: _filter == 'all',
            onSelected: (selected) {
              if (selected) {
                setState(() {
                  _filter = 'all';
                });
                _loadIssues();
              }
            },
          ),
          const SizedBox(width: 8),
          FilterChip(
            label: const Text('Open'),
            selected: _filter == 'open',
            onSelected: (selected) {
              if (selected) {
                setState(() {
                  _filter = 'open';
                });
                _loadIssues();
              }
            },
          ),
          const SizedBox(width: 8),
          FilterChip(
            label: const Text('Closed'),
            selected: _filter == 'closed',
            onSelected: (selected) {
              if (selected) {
                setState(() {
                  _filter = 'closed';
                });
                _loadIssues();
              }
            },
          ),
        ],
      ),
    );
  }

  Widget _buildIssueItem(Issue issue) {
    final dateFormat = DateFormat('MMM d, yyyy');
    
    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
      child: InkWell(
        onTap: () {
          context.push('/issue/${issue.number}');
        },
        borderRadius: BorderRadius.circular(6),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Icon(
                    issue.state == 'open' ? Icons.error_outline : Icons.check_circle_outline,
                    size: 20,
                    color: issue.state == 'open' ? AppTheme.issueOpen : AppTheme.issueClosed,
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    child: Text(
                      issue.title,
                      style: const TextStyle(
                        fontSize: 16,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 8),
              Row(
                children: [
                  Text(
                    '#${issue.number}',
                    style: TextStyle(
                      color: AppTheme.secondary,
                      fontSize: 14,
                    ),
                  ),
                  const SizedBox(width: 8),
                  Text(
                    'opened ${dateFormat.format(issue.createdAt)} by ${issue.user.login}',
                    style: TextStyle(
                      color: AppTheme.secondary,
                      fontSize: 14,
                    ),
                  ),
                  const Spacer(),
                  if (issue.comments > 0) ...[
                    Icon(
                      Icons.comment_outlined,
                      size: 16,
                      color: AppTheme.secondary,
                    ),
                    const SizedBox(width: 4),
                    Text(
                      '${issue.comments}',
                      style: TextStyle(
                        color: AppTheme.secondary,
                        fontSize: 14,
                      ),
                    ),
                  ],
                ],
              ),
              if (issue.labels.isNotEmpty) ...[
                const SizedBox(height: 8),
                Wrap(
                  spacing: 4,
                  runSpacing: 4,
                  children: issue.labels.map((label) {
                    return Container(
                      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                      decoration: BoxDecoration(
                        color: Color(int.parse('0xFF${label.color}')),
                        borderRadius: BorderRadius.circular(12),
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
                ),
              ],
            ],
          ),
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final repository = context.watch<RepositoryProvider>();

    return Scaffold(
      appBar: AppBar(
        title: Text(repository.fullName ?? 'Issues'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: _isLoading ? null : _loadIssues,
          ),
        ],
      ),
      body: Column(
        children: [
          if (repository.isSet) ...[
            Padding(
              padding: const EdgeInsets.symmetric(vertical: 8),
              child: _buildFilterChips(),
            ),
            const Divider(height: 1),
          ],
          Expanded(
            child: _buildContent(),
          ),
        ],
      ),
    );
  }

  Widget _buildContent() {
    final repository = context.watch<RepositoryProvider>();

    if (!repository.isSet) {
      return Center(
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
            TextButton(
              onPressed: () {
                context.push('/repository-settings');
              },
              child: const Text('Set Repository'),
            ),
          ],
        ),
      );
    }

    if (_isLoading && _issues.isEmpty) {
      return const Center(
        child: CircularProgressIndicator(),
      );
    }

    if (_error != null && _issues.isEmpty) {
      return Center(
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
              'Failed to load issues',
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
              onPressed: _loadIssues,
              child: const Text('Retry'),
            ),
          ],
        ),
      );
    }

    if (_issues.isEmpty) {
      return Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              Icons.inbox,
              size: 64,
              color: AppTheme.secondary,
            ),
            const SizedBox(height: 16),
            Text(
              'No issues found',
              style: TextStyle(
                fontSize: 18,
                color: AppTheme.secondary,
              ),
            ),
          ],
        ),
      );
    }

    return RefreshIndicator(
      onRefresh: _loadIssues,
      child: ListView.builder(
        controller: _scrollController,
        padding: const EdgeInsets.symmetric(vertical: 8),
        itemCount: _issues.length + (_hasMore ? 1 : 0),
        itemBuilder: (context, index) {
          if (index == _issues.length) {
            return const Padding(
              padding: EdgeInsets.all(16),
              child: Center(
                child: CircularProgressIndicator(),
              ),
            );
          }
          return _buildIssueItem(_issues[index]);
        },
      ),
    );
  }
}