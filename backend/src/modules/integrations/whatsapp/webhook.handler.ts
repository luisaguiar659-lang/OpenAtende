export interface WhatsAppWebhookPayload {
  phone: string;
  text: string;
  timestamp?: string;
}

export function handleWhatsAppWebhook(payload: WhatsAppWebhookPayload) {
  return {
    event: 'message.received',
    channel: 'WHATSAPP',
    phone: payload.phone,
    text: payload.text,
    receivedAt: payload.timestamp ?? new Date().toISOString(),
  };
}
