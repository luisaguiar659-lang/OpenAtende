export interface Customer {
  id: string;
  name: string;
  phone?: string;
  email?: string;
}

const customers: Customer[] = [];

export function createCustomer(customer: Customer) {
  customers.push(customer);
  return customer;
}

export function listCustomers() {
  return customers;
}
