import { apiClient, refreshSessionSingleFlight } from '../../../shared/api/apiClient.js';

export async function login({ email, password }) {
  const response = await apiClient.post('/auth/login', { email, password });
  return response.data.data;
}

export async function refreshSession(refreshToken) {
  return refreshSessionSingleFlight(refreshToken);
}

export async function logout(accessToken) {
  await apiClient.post('/auth/logout', {}, {
    headers: { Authorization: `Bearer ${accessToken}` },
    _baseplusSkipAuthRefresh: true,
  });
}

export async function changeInitialPassword(payload) {
  const response = await apiClient.post('/auth/change-initial-password', payload);
  return response.data;
}

export async function getMe() {
  const response = await apiClient.get('/auth/me');
  return response.data.data;
}
