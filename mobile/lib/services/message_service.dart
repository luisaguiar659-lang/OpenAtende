import '../models/message.dart';

class MessageService {
  final List<Message> _messages = [
    Message(
      text: 'Olá, preciso de ajuda',
      sender: 'client',
      time: '10:32',
    ),
    Message(
      text: 'Olá! Como posso ajudar?',
      sender: 'agent',
      time: '10:33',
    ),
  ];

  List<Message> getMessages() {
    return _messages;
  }

  void sendMessage(String text) {
    _messages.add(
      Message(
        text: text,
        sender: 'agent',
        time: 'agora',
      ),
    );
  }
}
