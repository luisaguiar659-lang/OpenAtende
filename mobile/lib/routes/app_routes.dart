import 'package:flutter/material.dart';
import '../screens/conversations_screen.dart';
import '../screens/login_screen.dart';

class AppRoutes {
  static Map<String, WidgetBuilder> routes = {
    '/login': (context) => const LoginScreen(),
    '/conversations': (context) => const ConversationsScreen(),
  };
}
