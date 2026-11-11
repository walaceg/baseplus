const ACCESS_TOKEN_KEY = 'baseplus.accessToken';
const REFRESH_TOKEN_KEY = 'baseplus.refreshToken';
const SESSION_ID_KEY = 'baseplus.authSessionId';

function createSessionId() {
  return window.crypto.randomUUID();
}

function legacySessionId(seed) {
  let first = 14695981039346656037n;
  let second = 7809847782465536322n;
  for (let index = 0; index < seed.length; index += 1) {
    const code = BigInt(seed.charCodeAt(index));
    first = BigInt.asUintN(64, (first ^ code) * 1099511628211n);
    second = BigInt.asUintN(64, (second ^ (code + 1n)) * 14029467366897019727n);
  }
  return `legacy-${first.toString(16)}-${second.toString(16)}`;
}

export const tokenStorage = {
  getAccessToken() {
    return window.localStorage.getItem(ACCESS_TOKEN_KEY);
  },
  getRefreshToken() {
    return window.localStorage.getItem(REFRESH_TOKEN_KEY);
  },
  getSessionId() {
    return window.localStorage.getItem(SESSION_ID_KEY);
  },
  ensureSessionId() {
    const current = this.getSessionId();
    if (current) return current;
    const seed = this.getRefreshToken() || this.getAccessToken();
    const sessionId = seed ? legacySessionId(seed) : createSessionId();
    window.localStorage.setItem(SESSION_ID_KEY, sessionId);
    return sessionId;
  },
  setAccessToken(token) {
    window.localStorage.setItem(ACCESS_TOKEN_KEY, token);
  },
  setRefreshToken(token) {
    window.localStorage.setItem(REFRESH_TOKEN_KEY, token);
  },
  setTokens({ accessToken, refreshToken, sessionId }) {
    if (sessionId) {
      window.localStorage.setItem(SESSION_ID_KEY, sessionId);
    }
    if (refreshToken) {
      this.setRefreshToken(refreshToken);
    }

    if (accessToken) {
      this.setAccessToken(accessToken);
    }
  },
  clear() {
    window.localStorage.removeItem(ACCESS_TOKEN_KEY);
    window.localStorage.removeItem(REFRESH_TOKEN_KEY);
    window.localStorage.removeItem(SESSION_ID_KEY);
  },
};
