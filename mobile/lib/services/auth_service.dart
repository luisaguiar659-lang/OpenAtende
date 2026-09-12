import '../models/auth_session.dart';

class AuthService {
  AuthSession? _session;

  bool get isLoggedIn => _session != null;

  Future<bool> login(String email, String password) async {
    // Preparado para integração com API de autenticação.
    // Futuramente receberá JWT real do backend.
    if (email.isNotEmpty && password.isNotEmpty) {
      _session = AuthSession(
        token: 'temporary_token',
        email: email,
      );
      return true;
    }

    return false;
  }

  AuthSession? get session => _session;

  String? get token => _session?.token;

  void logout() {
    _session = null;
  }
}
