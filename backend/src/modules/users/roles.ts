export enum UserRole {
  ADMIN = "ADMIN",
  ATTENDANT = "ATTENDANT"
}

export function canManageUsers(role: string) {
  return role === UserRole.ADMIN;
}
