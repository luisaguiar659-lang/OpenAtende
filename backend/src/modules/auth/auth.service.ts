import bcrypt from 'bcryptjs';
import jwt from 'jsonwebtoken';

export async function createPassword(password: string) {
  return bcrypt.hash(password, 10);
}

export function generateToken(userId: string) {
  return jwt.sign(
    { userId },
    process.env.JWT_SECRET || 'openatende-secret',
    { expiresIn: '7d' }
  );
}
