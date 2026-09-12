class ApiService {
  final String baseUrl;

  ApiService({this.baseUrl = 'https://api.openatende.com'});

  Future<void> connect() async {
    // Preparado para integração HTTP com o backend.
  }

  Future<List<dynamic>> getConversations() async {
    // Futuramente buscará conversas reais da API.
    return [];
  }

  Future<void> sendMessage(String conversationId, String message) async {
    // Futuramente enviará mensagens para o servidor.
  }
}
