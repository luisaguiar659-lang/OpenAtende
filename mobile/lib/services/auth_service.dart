class AuthService {
  String? _token;

  bool get isLoggedIn => _token != null;

  Future<bool> login(String email, String password) async {
    // Preparado para integração com API de autenticação.
    // Futuramente receberá JWT do backend.
    if (email.isNotEmpty && password.isNotEmpty) {
      _token = 'temporary_token';
      return true;
    }

    return false;
  }

  String? get token => _token;

  void logout() {
    _token = null;
  }
}
