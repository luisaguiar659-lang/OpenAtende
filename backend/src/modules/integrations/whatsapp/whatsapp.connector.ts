export class WhatsappConnector {
  async receiveMessage(payload: unknown) {
    return {
      channel: "WHATSAPP",
      payload,
    };
  }

  async sendMessage(phone: string, message: string) {
    return {
      sent: true,
      phone,
      message,
    };
  }
}
