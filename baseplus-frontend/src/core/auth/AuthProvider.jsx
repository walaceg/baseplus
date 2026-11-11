import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { AuthContext } from './AuthContext.js';
import { isDefinitiveRefreshRejection, setUnauthorizedHandler, withAuthSessionLock } from '../../shared/api/apiClient.js';
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
  const authSessionIdRef = useRef(tokenStorage.getSessionId());
  const authStateGenerationRef = useRef(0);

  const clearAuthState = useCallback(async ({ storageAlreadyCleared = false, expectedRefreshToken } = {}) => {
    if (!storageAlreadyCleared) {
      const cleared = await withAuthSessionLock(() => {
        if (expectedRefreshToken !== undefined && tokenStorage.getRefreshToken() !== expectedRefreshToken) {
          return false;
        }
        tokenStorage.clear();
        return true;
      });
      if (!cleared) {
        return false;
      }
    }

    authStateGenerationRef.current += 1;
    authSessionIdRef.current = null;
    setAccessToken(null);
    setUser(null);
    setMustChangePassword(false);
    setIsLoading(false);
    setMode(null);
    setDensity(null);
    return true;
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
    const authStorageKeys = new Set([
      'baseplus.accessToken',
      'baseplus.refreshToken',
      'baseplus.authSessionId',
    ]);

    const synchronizeAuthState = (event) => {
      if (event.key !== null && !authStorageKeys.has(event.key)) {
        return;
      }

      // Read the shared current snapshot; queued storage events may be stale.
      const currentAccessToken = tokenStorage.getAccessToken();
      const currentRefreshToken = tokenStorage.getRefreshToken();
      const currentSessionId = tokenStorage.getSessionId();

      if (!currentAccessToken || !currentRefreshToken || !currentSessionId) {
        void clearAuthState({ storageAlreadyCleared: true });
        return;
      }

      if (currentSessionId === authSessionIdRef.current) {
        setAccessToken(currentAccessToken);
        return;
      }

      const generation = ++authStateGenerationRef.current;
      authSessionIdRef.current = currentSessionId;
      setAccessToken(currentAccessToken);
      setUser(null);
      setMustChangePassword(false);
      setMode(null);
      setDensity(null);
      setIsLoading(true);

      void (async () => {
        try {
          const currentUser = await authService.getMe();
          const preferences = currentUser.mustChangePassword
            ? null
            : await contaService.getPreferencias().catch(() => null);

          if (!isMounted || generation !== authStateGenerationRef.current ||
              tokenStorage.getSessionId() !== currentSessionId) {
            return;
          }

          setAccessToken(tokenStorage.getAccessToken());
          setUser(currentUser);
          setMustChangePassword(Boolean(currentUser.mustChangePassword));
          if (preferences) {
            setMode(preferences.tema);
            setDensity(preferences.preferenciaVisual);
          }
        } catch (error) {
          if (isMounted && generation === authStateGenerationRef.current &&
              tokenStorage.getSessionId() === currentSessionId &&
              isDefinitiveRefreshRejection(error)) {
            await clearAuthState({ expectedRefreshToken: currentRefreshToken });
          }
        } finally {
          if (isMounted && generation === authStateGenerationRef.current) {
            setIsLoading(false);
          }
        }
      })();
    };

    window.addEventListener('storage', synchronizeAuthState);
    return () => {
      isMounted = false;
      window.removeEventListener('storage', synchronizeAuthState);
    };
  }, [clearAuthState, setDensity, setMode]);

  useEffect(() => {
    let isMounted = true;

    async function restoreSession() {
      const storedAccessToken = tokenStorage.getAccessToken();
      const storedRefreshToken = tokenStorage.getRefreshToken();

      if (!storedAccessToken && !storedRefreshToken) {
        setIsLoading(false);
        return;
      }

      let refreshAttempted = false;
      try {
        const restoreSessionId = await withAuthSessionLock(() => tokenStorage.ensureSessionId());
        authSessionIdRef.current = restoreSessionId;
        if (!storedAccessToken && storedRefreshToken) {
          refreshAttempted = true;
          const refreshed = await authService.refreshSession(storedRefreshToken);
          refreshAttempted = false;
          const restoredAccessToken = refreshed.superseded
            ? tokenStorage.getAccessToken()
            : refreshed.token;
          if (!restoredAccessToken) {
            throw new Error('A sessão foi atualizada em outra aba.');
          }

          if (isMounted) {
            setAccessToken(restoredAccessToken);
          }
        }

        const currentUser = await authService.getMe();
        const preferences = currentUser.mustChangePassword ? null : await contaService.getPreferencias().catch(() => null);

        if (tokenStorage.getSessionId() !== restoreSessionId) {
          return;
        }

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
      } catch (error) {
        if (isMounted && refreshAttempted && isDefinitiveRefreshRejection(error)) {
          await clearAuthState({ expectedRefreshToken: storedRefreshToken });
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
    authStateGenerationRef.current += 1;
    const session = await authService.login({ email, password });

    const sessionId = window.crypto.randomUUID();
    await withAuthSessionLock(() => tokenStorage.setTokens({
      accessToken: session.token,
      refreshToken: session.refreshToken,
      sessionId,
    }));
    authSessionIdRef.current = sessionId;
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
    const logoutAccessToken = tokenStorage.getAccessToken();

    // Clear synchronously: an in-flight refresh must observe the invalidated session.
    tokenStorage.clear();
    await clearAuthState({ storageAlreadyCleared: true });

    if (logoutAccessToken) {
      await authService.logout(logoutAccessToken);
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
