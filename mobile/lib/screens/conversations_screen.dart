import 'package:flutter/material.dart';

class ConversationsScreen extends StatelessWidget {
  const ConversationsScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Conversas'),
      ),
      body: ListView(
        children: const [
          ListTile(
            leading: CircleAvatar(child: Text('J')),
            title: Text('João Silva'),
            subtitle: Text('Olá, preciso de ajuda'),
          ),
          ListTile(
            leading: CircleAvatar(child: Text('M')),
            title: Text('Maria'),
            subtitle: Text('Meu pedido chegou?'),
          ),
        ],
      ),
    );
  }
}
