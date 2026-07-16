import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../auth/useAuth.js';
import { useAuthorization } from '../auth/useAuthorization.js';

export function ProtectedRoute({ children, permissions = [] }) {
  const { isAuthenticated, isLoading, mustChangePassword } = useAuth();
  const { canAny } = useAuthorization();
  const location = useLocation();

  if (isLoading) {
    return <main className="route-status">Carregando...</main>;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />;
  }

  if (mustChangePassword && location.pathname !== '/change-initial-password') {
    return <Navigate to="/change-initial-password" replace />;
  }

  if (permissions.length > 0 && !canAny(permissions)) {
    return (
      <main className="route-status">
        <h1>Acesso não autorizado</h1>
        <p>Seu perfil não possui permissão para acessar esta área.</p>
      </main>
    );
  }

  return children;
}
