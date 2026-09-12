import { Router } from 'express';
import { createCustomer, listCustomers } from './customer.controller';

const router = Router();

router.get('/', listCustomers);
router.post('/', createCustomer);

export default router;
