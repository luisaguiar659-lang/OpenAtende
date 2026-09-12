import { Request, Response, NextFunction } from 'express';

export function errorMiddleware(
  err: Error,
  req: Request,
  res: Response,
  next: NextFunction
) {
  console.error(err.message);

  return res.status(500).json({
    error: 'Internal server error'
  });
}
