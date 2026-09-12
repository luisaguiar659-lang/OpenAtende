export interface Message {
  id: string;
  conversationId: string;
  content: string;
  sender: 'customer' | 'agent' | 'bot';
  createdAt: Date;
}
