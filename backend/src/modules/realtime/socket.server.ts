// OpenAtende realtime layer
// WebSocket server base

export interface RealtimeEvent {
  type: string;
  conversationId?: string;
  payload: unknown;
}

export function emitMessage(event: RealtimeEvent) {
  return {
    sent: true,
    event,
  };
}
