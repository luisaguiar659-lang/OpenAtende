export interface CreateTicketInput {
  customerId: string;
  subject: string;
  priority?: 'LOW' | 'MEDIUM' | 'HIGH';
}

export function validateTicket(input: CreateTicketInput) {
  if (!input.customerId) throw new Error('customerId is required');
  if (!input.subject) throw new Error('subject is required');

  return {
    ...input,
    priority: input.priority ?? 'MEDIUM'
  };
}
