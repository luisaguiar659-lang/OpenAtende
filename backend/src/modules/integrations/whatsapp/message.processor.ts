export interface IncomingMessage {
  phone: string;
  text: string;
}

export class WhatsAppMessageProcessor {
  async process(message: IncomingMessage) {
    return {
      customerPhone: message.phone,
      content: message.text,
      status: 'received'
    };
  }
}
