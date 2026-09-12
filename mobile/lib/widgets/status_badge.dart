import 'package:flutter/material.dart';

class StatusBadge extends StatelessWidget {
  final String text;
  final bool online;

  const StatusBadge({super.key, required this.text, required this.online});

  @override
  Widget build(BuildContext context) {
    return Chip(
      avatar: CircleAvatar(
        radius: 5,
        backgroundColor: online ? Colors.green : Colors.grey,
      ),
      label: Text(text),
    );
  }
}
