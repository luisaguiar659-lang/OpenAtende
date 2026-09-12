class AuthSession {
  final String token;
  final String email;

  AuthSession({
    required this.token,
    required this.email,
  });

  factory AuthSession.fromJson(Map<String, dynamic> json) {
    return AuthSession(
      token: json['token'] ?? '',
      email: json['email'] ?? '',
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'token': token,
      'email': email,
    };
  }
}
