import 'package:flutter/material.dart';

class MessageBubble extends StatelessWidget {
  final String text;
  final String time;
  final bool isAgent;

  const MessageBubble({
    super.key,
    required this.text,
    required this.time,
    required this.isAgent,
  });

  @override
  Widget build(BuildContext context) {
    return Align(
      alignment: isAgent ? Alignment.centerRight : Alignment.centerLeft,
      child: Container(
        margin: const EdgeInsets.symmetric(vertical: 5),
        padding: const EdgeInsets.all(12),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.end,
          children: [
            Text(text),
            Text(time),
          ],
        ),
      ),
    );
  }
}
