import { createConversation, listConversations } from './conversation.service';

export function getConversations() {
  return listConversations();
}

export function openConversation(data: any) {
  return createConversation({
    customerId: data.customerId,
    status: 'OPEN',
    channel: data.channel ?? 'WEB'
  });
}
