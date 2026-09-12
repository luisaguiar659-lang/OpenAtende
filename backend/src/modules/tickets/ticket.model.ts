export interface Ticket {
  id: string;
  customerId: string;
  assignedUserId?: string;
  status: 'OPEN' | 'PENDING' | 'CLOSED';
  priority: 'LOW' | 'MEDIUM' | 'HIGH';
  createdAt: Date;
}
