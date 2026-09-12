export type MessagePayload = {
  conversationId: string;
  sender: string;
  content: string;
  channel: string;
};

export class MessagePipeline {
  async process(payload: MessagePayload) {
    return {
      accepted: true,
      event: 'message.received',
      data: payload
    };
  }
}
