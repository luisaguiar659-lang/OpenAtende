export interface UserSession {
  token: string;
  user: {
    id: string;
    name: string;
    role: string;
  };
}

export function saveSession(session: UserSession) {
  localStorage.setItem('openatende_session', JSON.stringify(session));
}

export function getSession(): UserSession | null {
  const data = localStorage.getItem('openatende_session');
  return data ? JSON.parse(data) : null;
}

export function clearSession() {
  localStorage.removeItem('openatende_session');
}
