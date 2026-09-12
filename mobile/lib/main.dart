import 'package:flutter/material.dart';
import 'routes/app_routes.dart';
import 'screens/splash_screen.dart';
import 'theme/app_theme.dart';

void main() {
  runApp(const OpenAtendeApp());
}

class OpenAtendeApp extends StatelessWidget {
  const OpenAtendeApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'OpenAtende',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.theme,
      routes: AppRoutes.routes,
      home: const SplashScreen(),
    );
  }
}
