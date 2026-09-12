export type MessageEvent = {
  conversationId: string;
  sender: string;
  content: string;
};

export class MessageDispatcher {
  dispatch(event: MessageEvent) {
    return {
      accepted: true,
      event: 'message.received',
      payload: event,
    };
  }
}
