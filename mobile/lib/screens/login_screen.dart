import 'package:flutter/material.dart';
import '../services/storage_service.dart';
import '../widgets/app_logo.dart';

class LoginScreen extends StatefulWidget {
  const LoginScreen({super.key});

  @override
  State<LoginScreen> createState() => _LoginScreenState();
}

class _LoginScreenState extends State<LoginScreen> {
  final emailController = TextEditingController();
  final passwordController = TextEditingController();
  bool loading = false;

  Future<void> login() async {
    setState(() => loading = true);

    await StorageService().saveSession(
      'demo-token',
      emailController.text,
    );

    if (!mounted) return;
    Navigator.pushReplacementNamed(context, '/conversations');
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const AppLogo(),
              const SizedBox(height: 40),
              TextField(controller: emailController, decoration: const InputDecoration(labelText: 'Email')),
              TextField(controller: passwordController, obscureText: true, decoration: const InputDecoration(labelText: 'Senha')),
              const SizedBox(height: 20),
              ElevatedButton(
                onPressed: loading ? null : login,
                child: Text(loading ? 'ENTRANDO...' : 'ENTRAR'),
              )
            ],
          ),
        ),
      ),
    );
  }
}
