class Message {
  final String text;
  final String sender;
  final String time;

  bool get isAgent => sender == 'agent';

  Message({
    required this.text,
    required this.sender,
    required this.time,
  });
}
