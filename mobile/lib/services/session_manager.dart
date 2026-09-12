import 'auth_service.dart';
import '../models/auth_session.dart';

class SessionManager {
  final AuthService _authService = AuthService();

  Future<bool> hasSession() async {
    return _authService.isLoggedIn;
  }

  Future<AuthSession?> getCurrentSession() async {
    if (!_authService.isLoggedIn) {
      return null;
    }

    return AuthSession(
      token: _authService.token ?? '',
      email: '',
    );
  }

  Future<void> clearSession() async {
    _authService.logout();
  }
}
