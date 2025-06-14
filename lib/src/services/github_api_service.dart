import 'package:dio/dio.dart';
import 'package:git_issue_app/src/config/app_config.dart';
import 'package:git_issue_app/src/services/secure_storage_service.dart';

class GitHubApiService {
  static const String _baseUrl = 'https://api.github.com';
  
  late final Dio _dio;
  
  GitHubApiService() {
    _dio = Dio(BaseOptions(
      baseUrl: _baseUrl,
      headers: {
        'Accept': 'application/vnd.github.v3+json',
      },
    ));
    
    _dio.interceptors.add(InterceptorsWrapper(
      onRequest: (options, handler) async {
        final token = await SecureStorageService.getToken();
        if (token != null) {
          options.headers['Authorization'] = 'Bearer $token';
        }
        handler.next(options);
      },
    ));
  }

  // Authentication methods
  Future<Map<String, dynamic>> validateToken(String token) async {
    try {
      final response = await _dio.get(
        '/user',
        options: Options(
          headers: {'Authorization': 'Bearer $token'},
        ),
      );
      return response.data;
    } on DioException catch (e) {
      if (e.response?.statusCode == 401) {
        throw Exception('Invalid token');
      }
      throw Exception('Failed to validate token: ${e.message}');
    }
  }

  Future<Map<String, dynamic>> initiateDeviceFlow() async {
    try {
      final response = await _dio.post(
        'https://github.com/login/device/code',
        data: {
          'client_id': AppConfig.githubClientId,
          'scope': 'repo',
        },
        options: Options(
          headers: {'Accept': 'application/json'},
        ),
      );
      return response.data;
    } catch (e) {
      throw Exception('Failed to initiate device flow: $e');
    }
  }

  Future<Map<String, dynamic>?> pollForToken(String deviceCode) async {
    try {
      final response = await _dio.post(
        'https://github.com/login/oauth/access_token',
        data: {
          'client_id': AppConfig.githubClientId,
          'device_code': deviceCode,
          'grant_type': 'urn:ietf:params:oauth:grant-type:device_code',
        },
        options: Options(
          headers: {'Accept': 'application/json'},
        ),
      );
      
      if (response.data['error'] == 'authorization_pending') {
        return null;
      }
      
      if (response.data['access_token'] != null) {
        return response.data;
      }
      
      throw Exception(response.data['error_description'] ?? 'Unknown error');
    } catch (e) {
      throw Exception('Failed to poll for token: $e');
    }
  }

  // Issue operations
  Future<List<dynamic>> getIssues(String owner, String repo, {String state = 'all', int page = 1}) async {
    try {
      final response = await _dio.get(
        '/repos/$owner/$repo/issues',
        queryParameters: {
          'state': state,
          'page': page,
          'per_page': 30,
        },
      );
      return response.data;
    } catch (e) {
      throw Exception('Failed to fetch issues: $e');
    }
  }

  Future<Map<String, dynamic>> getIssue(String owner, String repo, int issueNumber) async {
    try {
      final response = await _dio.get('/repos/$owner/$repo/issues/$issueNumber');
      return response.data;
    } catch (e) {
      throw Exception('Failed to fetch issue: $e');
    }
  }

  Future<List<dynamic>> getIssueComments(String owner, String repo, int issueNumber) async {
    try {
      final response = await _dio.get('/repos/$owner/$repo/issues/$issueNumber/comments');
      return response.data;
    } catch (e) {
      throw Exception('Failed to fetch comments: $e');
    }
  }

  Future<Map<String, dynamic>> createIssue(String owner, String repo, {
    required String title,
    String? body,
    List<String>? labels,
  }) async {
    try {
      final data = {
        'title': title,
        if (body != null) 'body': body,
        if (labels != null) 'labels': labels,
      };
      
      final response = await _dio.post(
        '/repos/$owner/$repo/issues',
        data: data,
      );
      return response.data;
    } catch (e) {
      throw Exception('Failed to create issue: $e');
    }
  }

  Future<Map<String, dynamic>> createComment(String owner, String repo, int issueNumber, String body) async {
    try {
      final response = await _dio.post(
        '/repos/$owner/$repo/issues/$issueNumber/comments',
        data: {'body': body},
      );
      return response.data;
    } catch (e) {
      throw Exception('Failed to create comment: $e');
    }
  }

  Future<List<dynamic>> getLabels(String owner, String repo) async {
    try {
      final response = await _dio.get('/repos/$owner/$repo/labels');
      return response.data;
    } catch (e) {
      throw Exception('Failed to fetch labels: $e');
    }
  }
}