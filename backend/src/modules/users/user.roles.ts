export const USER_ROLES = {
  ADMIN: "admin",
  AGENT: "agent",
  SUPERVISOR: "supervisor",
} as const;

export type UserRole = typeof USER_ROLES[keyof typeof USER_ROLES];

export function canManageUsers(role: UserRole) {
  return role === USER_ROLES.ADMIN;
}

export function canAttendConversations(role: UserRole) {
  return role === USER_ROLES.ADMIN || role === USER_ROLES.AGENT || role === USER_ROLES.SUPERVISOR;
}
