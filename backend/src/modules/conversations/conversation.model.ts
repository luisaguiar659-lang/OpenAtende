export interface Conversation {
  id: string;
  customerId: string;
  status: 'open' | 'closed' | 'pending';
  channel: 'whatsapp' | 'telegram' | 'web';
  createdAt: Date;
}
