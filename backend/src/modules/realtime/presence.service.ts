type UserStatus = "online" | "offline" | "busy";

const users = new Map<string, UserStatus>();

export function setUserStatus(userId: string, status: UserStatus) {
  users.set(userId, status);
}

export function getUserStatus(userId: string) {
  return users.get(userId) ?? "offline";
}

export function getOnlineUsers() {
  return Array.from(users.entries());
}
