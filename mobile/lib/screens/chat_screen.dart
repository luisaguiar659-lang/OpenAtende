import 'package:flutter/material.dart';
import '../widgets/message_bubble.dart';
import '../services/message_service.dart';

class ChatScreen extends StatefulWidget {
  const ChatScreen({super.key});

  @override
  State<ChatScreen> createState() => _ChatScreenState();
}

class _ChatScreenState extends State<ChatScreen> {
  final TextEditingController controller = TextEditingController();
  final MessageService messageService = MessageService();

  void sendMessage() {
    if (controller.text.trim().isEmpty) return;

    setState(() {
      messageService.sendMessage(controller.text);
      controller.clear();
    });
  }

  @override
  Widget build(BuildContext context) {
    final messages = messageService.getMessages();

    return Scaffold(
      appBar: AppBar(title: const Text('Atendimento')),
      body: Column(
        children: [
          Expanded(
            child: ListView.builder(
              padding: const EdgeInsets.all(12),
              itemCount: messages.length,
              itemBuilder: (context, index) {
                final message = messages[index];

                return MessageBubble(
                  text: message.text,
                  time: message.time,
                  isAgent: message.sender == 'agent',
                );
              },
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(12),
            child: Row(
              children: [
                Expanded(
                  child: TextField(
                    controller: controller,
                    decoration: const InputDecoration(
                      hintText: 'Digite uma mensagem...',
                      border: OutlineInputBorder(),
                    ),
                  ),
                ),
                IconButton(
                  onPressed: sendMessage,
                  icon: const Icon(Icons.send),
                )
              ],
            ),
          )
        ],
      ),
    );
  }
}
