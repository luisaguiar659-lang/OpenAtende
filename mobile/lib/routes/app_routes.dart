import 'package:flutter/material.dart';
import '../screens/conversations_screen.dart';

class AppRoutes {
  static Map<String, WidgetBuilder> routes = {
    '/conversations': (context) => const ConversationsScreen(),
  };
}
