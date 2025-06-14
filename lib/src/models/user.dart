class User {
  final String login;
  final String? name;
  final String? avatarUrl;
  final String? bio;
  final String? company;
  final String? location;
  final String? email;
  final int publicRepos;
  final int followers;
  final int following;

  User({
    required this.login,
    this.name,
    this.avatarUrl,
    this.bio,
    this.company,
    this.location,
    this.email,
    required this.publicRepos,
    required this.followers,
    required this.following,
  });

  factory User.fromJson(Map<String, dynamic> json) {
    return User(
      login: json['login'],
      name: json['name'],
      avatarUrl: json['avatar_url'],
      bio: json['bio'],
      company: json['company'],
      location: json['location'],
      email: json['email'],
      publicRepos: json['public_repos'] ?? 0,
      followers: json['followers'] ?? 0,
      following: json['following'] ?? 0,
    );
  }
}