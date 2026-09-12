export interface Message {
  id: string;
  conversationId: string;
  content: string;
  sender: 'customer' | 'agent' | 'bot';
}

const messages: Message[] = [];

export function sendMessage(message: Message) {
  messages.push(message);
  return message;
}

export function listMessages(conversationId: string) {
  return messages.filter((item) => item.conversationId === conversationId);
}
