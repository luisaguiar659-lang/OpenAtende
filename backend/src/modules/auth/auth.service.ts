import bcrypt from 'bcryptjs';
import jwt from 'jsonwebtoken';

export interface UserSession {
  id: string;
  name: string;
  role: 'ADMIN' | 'AGENT';
}

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

export function createSession(user: UserSession) {
  return {
    user,
    token: generateToken(user.id)
  };
}
