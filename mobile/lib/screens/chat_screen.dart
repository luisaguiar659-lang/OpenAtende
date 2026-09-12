import 'package:flutter/material.dart';

class ChatScreen extends StatelessWidget {
  const ChatScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Atendimento'),
      ),
      body: Column(
        children: [
          const Expanded(
            child: Center(
              child: Text('Histórico da conversa'),
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(12),
            child: TextField(
              decoration: const InputDecoration(
                hintText: 'Digite uma mensagem...',
                border: OutlineInputBorder(),
              ),
            ),
          )
        ],
      ),
    );
  }
}
