import { PropsWithChildren } from 'react';
import { Navigate } from 'react-router-dom';

type ProtectedRouteProps = PropsWithChildren<{ requiredRole?: 'ADMIN' | 'USER' }>;

export default function ProtectedRoute({ children, requiredRole }: ProtectedRouteProps) {
  const token = localStorage.getItem('token');
  if (!token) {
    return <Navigate to="/login" replace />;
  }

  if (requiredRole && (localStorage.getItem('role') ?? 'USER') !== requiredRole) {
    return <Navigate to="/" replace />;
  }

  return children;
}