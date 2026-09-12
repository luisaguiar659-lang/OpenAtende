export interface WhatsAppPayload {
  phone: string;
  text: string;
  timestamp?: string;
}

export function mapWhatsAppMessage(payload: WhatsAppPayload) {
  return {
    customerPhone: payload.phone,
    content: payload.text,
    channel: 'WHATSAPP',
    receivedAt: payload.timestamp ?? new Date().toISOString()
  };
}
