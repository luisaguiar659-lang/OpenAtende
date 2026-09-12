export class NotificationService {
  send(event: string, payload: unknown) {
    return {
      event,
      payload,
      sentAt: new Date().toISOString()
    };
  }
}
