import { ReactNode } from 'react';
import { getSession } from '../services/auth';

type Props = {
  children: ReactNode;
};

export default function ProtectedRoute({ children }: Props) {
  const session = getSession();

  if (!session?.token) {
    return null;
  }

  return children;
}
