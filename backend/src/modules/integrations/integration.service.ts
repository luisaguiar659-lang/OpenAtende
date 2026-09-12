export type Channel = 'WHATSAPP' | 'TELEGRAM' | 'WEB';

export interface IncomingMessage {
  channel: Channel;
  customerId: string;
  content: string;
}

export class IntegrationService {
  async receive(message: IncomingMessage) {
    return {
      received: true,
      channel: message.channel,
      content: message.content,
    };
  }
}
