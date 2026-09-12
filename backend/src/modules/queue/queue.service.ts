export interface QueueItem {
  customerId: string;
  conversationId: string;
  priority: 'LOW' | 'NORMAL' | 'HIGH';
}

const queue: QueueItem[] = [];

export function addToQueue(item: QueueItem) {
  queue.push(item);
  return queue;
}

export function getQueue() {
  return queue;
}
