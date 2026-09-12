export class MessageProcessor {
  process(payload: { from: string; text: string }) {
    return {
      sender: payload.from,
      content: payload.text,
      receivedAt: new Date()
    };
  }
}
