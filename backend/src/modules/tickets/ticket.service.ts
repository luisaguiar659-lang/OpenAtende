export interface TicketData {
  customerId: string;
  subject: string;
  priority: 'LOW' | 'MEDIUM' | 'HIGH';
}

export function createTicket(data: TicketData) {
  return {
    ...data,
    status: 'OPEN',
    createdAt: new Date()
  };
}
