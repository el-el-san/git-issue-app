class Issue {
  final int number;
  final String title;
  final String? body;
  final String state;
  final DateTime createdAt;
  final DateTime? closedAt;
  final User user;
  final List<Label> labels;
  final int comments;

  Issue({
    required this.number,
    required this.title,
    this.body,
    required this.state,
    required this.createdAt,
    this.closedAt,
    required this.user,
    required this.labels,
    required this.comments,
  });

  factory Issue.fromJson(Map<String, dynamic> json) {
    return Issue(
      number: json['number'],
      title: json['title'],
      body: json['body'],
      state: json['state'],
      createdAt: DateTime.parse(json['created_at']),
      closedAt: json['closed_at'] != null ? DateTime.parse(json['closed_at']) : null,
      user: User.fromJson(json['user']),
      labels: (json['labels'] as List).map((e) => Label.fromJson(e)).toList(),
      comments: json['comments'] ?? 0,
    );
  }
}

class User {
  final String login;
  final String avatarUrl;

  User({required this.login, required this.avatarUrl});

  factory User.fromJson(Map<String, dynamic> json) {
    return User(
      login: json['login'],
      avatarUrl: json['avatar_url'],
    );
  }
}

class Label {
  final String name;
  final String color;
  final String? description;

  Label({required this.name, required this.color, this.description});

  factory Label.fromJson(Map<String, dynamic> json) {
    return Label(
      name: json['name'],
      color: json['color'],
      description: json['description'],
    );
  }
}

class Comment {
  final int id;
  final String body;
  final User user;
  final DateTime createdAt;

  Comment({
    required this.id,
    required this.body,
    required this.user,
    required this.createdAt,
  });

  factory Comment.fromJson(Map<String, dynamic> json) {
    return Comment(
      id: json['id'],
      body: json['body'],
      user: User.fromJson(json['user']),
      createdAt: DateTime.parse(json['created_at']),
    );
  }
}