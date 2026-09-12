import { Request, Response } from 'express';
import { customerService } from './customer.service';

export function listCustomers(req: Request, res: Response) {
  const customers = customerService.list();
  return res.json(customers);
}

export function createCustomer(req: Request, res: Response) {
  const customer = customerService.create(req.body);
  return res.status(201).json(customer);
}
