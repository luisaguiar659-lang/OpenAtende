import 'package:flutter/material.dart';

void main() {
  runApp(const OpenAtendeApp());
}

class OpenAtendeApp extends StatelessWidget {
  const OpenAtendeApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'OpenAtende',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.blue),
        useMaterial3: true,
      ),
      home: const LoginPage(),
    );
  }
}

class LoginPage extends StatelessWidget {
  const LoginPage({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            const Text('OPENATENDE', style: TextStyle(fontSize: 28)),
            const SizedBox(height: 30),
            const TextField(decoration: InputDecoration(labelText: 'Email')),
            const TextField(decoration: InputDecoration(labelText: 'Senha'), obscureText: true),
            const SizedBox(height: 20),
            ElevatedButton(
              onPressed: () {},
              child: const Text('ENTRAR'),
            )
          ],
        ),
      ),
    );
  }
}
