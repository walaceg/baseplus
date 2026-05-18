const ACCESS_TOKEN_KEY = 'baseplus.accessToken';
const REFRESH_TOKEN_KEY = 'baseplus.refreshToken';

export const tokenStorage = {
  getAccessToken() {
    return window.localStorage.getItem(ACCESS_TOKEN_KEY);
  },
  getRefreshToken() {
    return window.localStorage.getItem(REFRESH_TOKEN_KEY);
  },
  setAccessToken(token) {
    window.localStorage.setItem(ACCESS_TOKEN_KEY, token);
  },
  setRefreshToken(token) {
    window.localStorage.setItem(REFRESH_TOKEN_KEY, token);
  },
  setTokens({ accessToken, refreshToken }) {
    if (accessToken) {
      this.setAccessToken(accessToken);
    }

    if (refreshToken) {
      this.setRefreshToken(refreshToken);
    }
  },
  clear() {
    window.localStorage.removeItem(ACCESS_TOKEN_KEY);
    window.localStorage.removeItem(REFRESH_TOKEN_KEY);
  },
};
