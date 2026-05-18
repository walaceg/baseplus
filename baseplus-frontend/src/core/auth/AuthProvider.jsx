import { useCallback, useEffect, useMemo, useState } from 'react';
import { AuthContext } from './AuthContext.js';
import { setUnauthorizedHandler } from '../../shared/api/apiClient.js';
import { tokenStorage } from '../../shared/storage/tokenStorage.js';
import { useTheme } from '../theme/useTheme.js';
import * as authService from '../../modules/auth/services/authService.js';
import * as contaService from '../../modules/conta/contaService.js';

export function AuthProvider({ children }) {
  const { setDensity, setMode } = useTheme();
  const [accessToken, setAccessToken] = useState(() => tokenStorage.getAccessToken());
  const [user, setUser] = useState(null);
  const [mustChangePassword, setMustChangePassword] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  const clearAuthState = useCallback(() => {
    tokenStorage.clear();
    setAccessToken(null);
    setUser(null);
    setMustChangePassword(false);
    setMode(null);
    setDensity(null);
  }, [setDensity, setMode]);

  const updateUser = useCallback((nextUser) => {
    setUser(nextUser);
  }, []);

  useEffect(() => {
    setUnauthorizedHandler(clearAuthState);

    return () => setUnauthorizedHandler(null);
  }, [clearAuthState]);

  useEffect(() => {
    let isMounted = true;

    async function restoreSession() {
      const storedAccessToken = tokenStorage.getAccessToken();
      const storedRefreshToken = tokenStorage.getRefreshToken();

      if (!storedAccessToken && !storedRefreshToken) {
        setIsLoading(false);
        return;
      }

      try {
        if (!storedAccessToken && storedRefreshToken) {
          const refreshed = await authService.refreshSession(storedRefreshToken);
          tokenStorage.setAccessToken(refreshed.token);

          if (isMounted) {
            setAccessToken(refreshed.token);
          }
        }

        const currentUser = await authService.getMe();
        const preferences = currentUser.mustChangePassword ? null : await contaService.getPreferencias().catch(() => null);

        if (isMounted) {
          setAccessToken(tokenStorage.getAccessToken());
          setUser(currentUser);
          setMustChangePassword(Boolean(currentUser.mustChangePassword));
          if (preferences) {
            setMode(preferences.tema);
            setDensity(preferences.preferenciaVisual);
          } else {
            setMode(null);
            setDensity(null);
          }
        }
      } catch {
        if (isMounted) {
          clearAuthState();
        }
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    }

    restoreSession();

    return () => {
      isMounted = false;
    };
  }, [clearAuthState, setDensity, setMode]);

  const signIn = useCallback(async ({ email, password }) => {
    const session = await authService.login({ email, password });

    tokenStorage.setTokens({
      accessToken: session.token,
      refreshToken: session.refreshToken,
    });
    setAccessToken(session.token);

    const currentUser = await authService.getMe();
    setUser(currentUser);
    setMustChangePassword(Boolean(session.mustChangePassword || currentUser.mustChangePassword));

    if (session.mustChangePassword || currentUser.mustChangePassword) {
      return { user: currentUser, mustChangePassword: true };
    }

    const preferences = await contaService.getPreferencias().catch(() => null);
    if (preferences) {
      setMode(preferences.tema);
      setDensity(preferences.preferenciaVisual);
    } else {
      setMode(null);
      setDensity(null);
    }

    return { user: currentUser, mustChangePassword: false };
  }, [setDensity, setMode]);

  const changeInitialPassword = useCallback(async ({ senhaAtual, novaSenha, confirmarNovaSenha }) => {
    const response = await authService.changeInitialPassword({ senhaAtual, novaSenha, confirmarNovaSenha });
    const currentUser = await authService.getMe();
    const preferences = await contaService.getPreferencias().catch(() => null);

    setUser(currentUser);
    setMustChangePassword(Boolean(currentUser.mustChangePassword));

    if (preferences) {
      setMode(preferences.tema);
      setDensity(preferences.preferenciaVisual);
    } else {
      setMode(null);
      setDensity(null);
    }

    return response;
  }, [setDensity, setMode]);

  const signOut = useCallback(async () => {
    try {
      if (tokenStorage.getAccessToken()) {
        await authService.logout();
      }
    } finally {
      clearAuthState();
    }
  }, [clearAuthState]);

  const value = useMemo(
    () => ({
      accessToken,
      isAuthenticated: Boolean(accessToken && user),
      isLoading,
      mustChangePassword,
      changeInitialPassword,
      signIn,
      signOut,
      user,
      updateUser,
    }),
    [accessToken, changeInitialPassword, isLoading, mustChangePassword, signIn, signOut, updateUser, user],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
