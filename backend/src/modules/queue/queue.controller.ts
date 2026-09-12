export class QueueController {
  async add(req: any, res: any) {
    return res.json({ message: 'Customer added to queue' });
  }

  async list(req: any, res: any) {
    return res.json([]);
  }
}
