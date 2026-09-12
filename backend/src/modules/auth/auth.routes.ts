import { Router } from 'express';

const router = Router();

router.post('/login', async (_req, res) => {
  res.json({
    message: 'Auth module ready',
  });
});

export default router;
