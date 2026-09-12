export type SocketEvent = {
  event: string;
  payload: unknown;
};

export class WebSocketGateway {
  private clients: Map<string, unknown> = new Map();

  connect(userId: string, socket: unknown) {
    this.clients.set(userId, socket);
  }

  disconnect(userId: string) {
    this.clients.delete(userId);
  }

  broadcast(event: SocketEvent) {
    return {
      delivered: this.clients.size,
      event: event.event,
    };
  }
}
