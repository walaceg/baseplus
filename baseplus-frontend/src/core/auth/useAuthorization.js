import { useMemo } from 'react';
import { canAny as checkAny, getAuthPermissions, getAuthRoles, hasPermission as checkPermission, hasRole as checkRole } from '../../shared/auth/permissions.js';
import { useAuth } from './useAuth.js';

export function useAuthorization() {
  const { accessToken, user } = useAuth();

  return useMemo(() => {
    const auth = { accessToken, user };

    return {
      roles: getAuthRoles(auth),
      permissions: getAuthPermissions(auth),
      hasRole: (role) => checkRole(auth, role),
      hasPermission: (permission) => checkPermission(auth, permission),
      canAny: (permissions) => checkAny(auth, permissions),
    };
  }, [accessToken, user]);
}
