export class BotService {
  processMessage(message: string) {
    const text = message.toLowerCase();

    if (text.includes('ola') || text.includes('olá')) {
      return 'Olá! Como posso ajudar você?';
    }

    return 'Recebi sua mensagem. Um atendente irá responder em breve.';
  }
}
