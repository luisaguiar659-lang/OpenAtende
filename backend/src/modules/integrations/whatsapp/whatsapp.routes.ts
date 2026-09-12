import { processWhatsAppWebhook } from "./whatsapp.webhook";

export async function whatsappWebhook(payload: any) {
  return processWhatsAppWebhook(payload);
}
