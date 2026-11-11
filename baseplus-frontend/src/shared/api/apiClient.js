import axios from 'axios';
import { tokenStorage } from '../storage/tokenStorage.js';

export const apiBaseURL = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

export const apiClient = axios.create({
  baseURL: apiBaseURL,
});

const refreshClient = axios.create({
  baseURL: apiBaseURL,
});

let refreshPromise = null;
let refreshPromiseToken = null;
let unauthorizedHandler = null;
const AUTH_SESSION_LOCK = 'baseplus-auth-session';

export function withAuthSessionLock(operation) {
  if (typeof navigator !== 'undefined' && navigator.locks?.request) {
    return navigator.locks.request(AUTH_SESSION_LOCK, operation);
  }

  return operation();
}

export function refreshSessionSingleFlight(expectedRefreshToken = tokenStorage.getRefreshToken()) {
  if (refreshPromise) {
    if (refreshPromiseToken === expectedRefreshToken) {
      return refreshPromise;
    }
    return refreshPromise.catch(() => null).then(() => {
      if (tokenStorage.getRefreshToken() !== expectedRefreshToken) {
        return { superseded: true };
      }
      return refreshSessionSingleFlight(expectedRefreshToken);
    });
  }

  refreshPromiseToken = expectedRefreshToken;
  const currentPromise = withAuthSessionLock(async () => {
    if (!expectedRefreshToken || tokenStorage.getRefreshToken() !== expectedRefreshToken) {
      return { superseded: true };
    }
    const sessionId = tokenStorage.ensureSessionId();

    const response = await refreshClient.post('/auth/refresh', { refreshToken: expectedRefreshToken });
    const data = response.data?.data;

    if (!data?.token || !data?.refreshToken) {
      throw new Error('Tokens ausentes na resposta de renovação.');
    }

    if (tokenStorage.getRefreshToken() !== expectedRefreshToken || tokenStorage.getSessionId() !== sessionId) {
      return { superseded: true };
    }

    tokenStorage.setTokens({ accessToken: data.token, refreshToken: data.refreshToken });
    return { ...data, superseded: false };
  }).finally(() => {
    if (refreshPromise === currentPromise) {
      refreshPromise = null;
      refreshPromiseToken = null;
    }
  });
  refreshPromise = currentPromise;

  return refreshPromise;
}

export function setUnauthorizedHandler(handler) {
  unauthorizedHandler = handler;
}

apiClient.interceptors.request.use((config) => {
  const token = tokenStorage.getAccessToken();
  config._baseplusRefreshToken = tokenStorage.getRefreshToken();
  config._baseplusAccessToken = token;
  config._baseplusSessionId = tokenStorage.getSessionId();

  if (token && !config._baseplusSkipAuthRefresh) {
    config.headers = config.headers ?? {};
    config.headers.Authorization = `Bearer ${token}`;
  }

  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    const status = error.response?.status;

    if (originalRequest?._baseplusSkipAuthRefresh) {
      return Promise.reject(error);
    }

    if (!originalRequest || status !== 401 || originalRequest._retry) {
      return Promise.reject(error);
    }

    const requestRefreshToken = originalRequest._baseplusRefreshToken;
    const refreshToken = tokenStorage.getRefreshToken();
    const sessionId = tokenStorage.getSessionId();

    if (isAuthEndpoint(originalRequest.url)) {
      await clearSession(requestRefreshToken);
      return Promise.reject(error);
    }

    if (requestRefreshToken && sessionId === originalRequest._baseplusSessionId &&
        refreshToken && refreshToken !== requestRefreshToken) {
      return retryWithCurrentAccessToken(originalRequest, error);
    }

    if (!refreshToken) {
      await clearSession(requestRefreshToken);
      return Promise.reject(error);
    }

    if (requestRefreshToken && requestRefreshToken !== refreshToken) {
      return Promise.reject(error);
    }

    originalRequest._retry = true;

    try {
      const refreshed = await refreshSessionSingleFlight(refreshToken);
      if (refreshed.superseded) {
        return retryWithCurrentAccessToken(originalRequest, error);
      }
      if (!refreshed.token) {
        return Promise.reject(error);
      }
      originalRequest.headers = originalRequest.headers ?? {};
      originalRequest.headers.Authorization = `Bearer ${refreshed.token}`;

      return apiClient(originalRequest);
    } catch (refreshError) {
      const currentRefreshToken = tokenStorage.getRefreshToken();
      if (currentRefreshToken && currentRefreshToken !== refreshToken && tokenStorage.getSessionId() === sessionId) {
        return retryWithCurrentAccessToken(originalRequest, error);
      }
      if (isDefinitiveRefreshRejection(refreshError) && currentRefreshToken === refreshToken && tokenStorage.getSessionId() === sessionId) {
        if (!hasWebLocks()) {
          const resolution = await waitForCrossTabRefresh(refreshToken, sessionId);
          if (resolution === 'rotated') {
            return retryWithCurrentAccessToken(originalRequest, error);
          }
          if (resolution === 'session-changed') {
            return Promise.reject(error);
          }
        }
        await clearSession(refreshToken);
      }
      return Promise.reject(refreshError);
    }
  },
);

async function clearSession(expectedRefreshToken) {
  const cleared = await withAuthSessionLock(() => {
    if (expectedRefreshToken !== undefined && tokenStorage.getRefreshToken() !== expectedRefreshToken) {
      return false;
    }
    tokenStorage.clear();
    return true;
  });

  if (cleared) {
    unauthorizedHandler?.({ storageAlreadyCleared: true });
  }
}

export function isDefinitiveRefreshRejection(error) {
  const status = error?.response?.status;
  return status === 401 || status === 403;
}

function hasWebLocks() {
  return typeof navigator !== 'undefined' && Boolean(navigator.locks?.request);
}

function waitForCrossTabRefresh(expectedRefreshToken, expectedSessionId) {
  return new Promise((resolve) => {
    let settled = false;
    const finish = (result) => {
      if (settled) return;
      settled = true;
      window.removeEventListener('storage', onStorage);
      window.clearTimeout(timeoutId);
      resolve(result);
    };
    const check = () => {
      if (tokenStorage.getSessionId() !== expectedSessionId) {
        finish('session-changed');
      } else if (tokenStorage.getRefreshToken() !== expectedRefreshToken) {
        finish('rotated');
      }
    };
    const onStorage = (event) => {
      if ([null, 'baseplus.authSessionId', 'baseplus.refreshToken'].includes(event.key)) check();
    };
    const timeoutId = window.setTimeout(() => finish('unchanged'), 2000);

    window.addEventListener('storage', onStorage);
    check();
  });
}

function retryWithCurrentAccessToken(originalRequest, originalError) {
  const currentRefreshToken = tokenStorage.getRefreshToken();
  const currentAccessToken = tokenStorage.getAccessToken();
  if (!currentRefreshToken || !currentAccessToken ||
      tokenStorage.getSessionId() !== originalRequest._baseplusSessionId) {
    return Promise.reject(originalError);
  }

  originalRequest._retry = true;
  originalRequest.headers = originalRequest.headers ?? {};
  originalRequest.headers.Authorization = `Bearer ${currentAccessToken}`;
  return apiClient(originalRequest);
}

function isAuthEndpoint(url = '') {
  return url.includes('/auth/login') || url.includes('/auth/refresh') || url.includes('/auth/logout');
}
