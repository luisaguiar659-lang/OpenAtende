class StorageService {
  String? _token;
  String? _email;

  Future<void> saveSession(String token, String email) async {
    _token = token;
    _email = email;
  }

  Future<String?> getToken() async {
    return _token;
  }

  Future<String?> getEmail() async {
    return _email;
  }

  Future<void> clearSession() async {
    _token = null;
    _email = null;
  }
}
