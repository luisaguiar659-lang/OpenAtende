import { Router } from 'express';
import { getConversations, openConversation } from './conversation.controller';

const router = Router();

router.get('/', (_req, res) => {
  res.json(getConversations());
});

router.post('/', (req, res) => {
  res.json(openConversation(req.body));
});

export default router;
