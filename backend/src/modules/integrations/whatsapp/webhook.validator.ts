export function validateWhatsAppWebhook(payload: unknown): boolean {
  if (!payload || typeof payload !== 'object') {
    return false;
  }

  const data = payload as Record<string, unknown>;

  return typeof data.phone === 'string' && typeof data.text === 'string';
}
