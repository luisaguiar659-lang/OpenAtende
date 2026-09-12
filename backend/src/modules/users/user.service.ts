export type UserRole = 'ADMIN' | 'AGENT';

export interface User {
  id: string;
  name: string;
  email: string;
  role: UserRole;
}

const users: User[] = [];

export function createUser(data: Omit<User, 'id'>) {
  const user: User = {
    id: crypto.randomUUID(),
    ...data,
  };

  users.push(user);
  return user;
}

export function listUsers() {
  return users;
}
