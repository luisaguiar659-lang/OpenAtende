import { Request, Response } from 'express';
import { messageService } from './message.service';

export function listMessages(req: Request, res: Response) {
  return res.json(messageService.list());
}

export function sendMessage(req: Request, res: Response) {
  return res.status(201).json(messageService.create(req.body));
}
