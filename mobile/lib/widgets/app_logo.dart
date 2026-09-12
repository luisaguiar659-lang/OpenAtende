import 'package:flutter/material.dart';
import '../constants/app_constants.dart';

class AppLogo extends StatelessWidget {
  const AppLogo({super.key});

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        const Icon(Icons.support_agent, size: 72),
        const SizedBox(height: 12),
        Text(
          AppConstants.appName.toUpperCase(),
          style: const TextStyle(
            fontSize: 28,
            fontWeight: FontWeight.bold,
          ),
        ),
        const Text(AppConstants.supportText),
      ],
    );
  }
}
