export interface WhatsAppWebhookPayload {
  from: string;
  message: string;
  timestamp: number;
}

export function processWhatsAppWebhook(payload: WhatsAppWebhookPayload) {
  return {
    channel: "WHATSAPP",
    customerPhone: payload.from,
    content: payload.message,
    receivedAt: new Date(payload.timestamp)
  };
}
