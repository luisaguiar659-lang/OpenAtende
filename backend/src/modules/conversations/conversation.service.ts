export interface Conversation {
  id: string;
  customerId: string;
  status: 'OPEN' | 'CLOSED' | 'PENDING';
  channel: 'WHATSAPP' | 'TELEGRAM' | 'WEB';
  createdAt: Date;
}

const conversations: Conversation[] = [];

export function createConversation(data: Omit<Conversation, 'id' | 'createdAt'>) {
  const conversation = {
    ...data,
    id: crypto.randomUUID(),
    createdAt: new Date()
  };

  conversations.push(conversation);
  return conversation;
}

export function listConversations() {
  return conversations;
}
