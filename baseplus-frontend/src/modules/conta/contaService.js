import { apiClient } from '../../shared/api/apiClient.js';

export async function getConta() {
  const response = await apiClient.get('/conta');
  return response.data.data;
}

export async function updateConta(payload) {
  const response = await apiClient.put('/conta', payload);
  return response.data.data;
}

export async function changeSenha(payload) {
  const response = await apiClient.post('/conta/senha', payload);
  return response.data;
}

export async function uploadAvatar(file) {
  const formData = new FormData();
  formData.append('file', file);

  const response = await apiClient.post('/conta/foto', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });

  return normalizeAvatar(response.data?.data);
}

export async function deleteAvatar() {
  const response = await apiClient.delete('/conta/foto');
  return normalizeAvatar(response.data?.data);
}

export async function getPreferencias() {
  const response = await apiClient.get('/conta/preferencias');
  return normalizePreferencias(response.data.data);
}

export async function updatePreferencias(payload) {
  const response = await apiClient.put('/conta/preferencias', buildPreferenciasPayload(payload));
  return normalizePreferencias(response.data.data);
}

export async function getSessoes() {
  const response = await apiClient.get('/conta/sessoes');
  return response.data.data;
}

function buildPreferenciasPayload(payload = {}) {
  return {
    tema: normalizeThemePreference(payload.tema ?? payload.userThemePreference),
    idioma: payload.idioma,
    notificacoes: Boolean(payload.notificacoes),
    corPrimaria: payload.corPrimaria,
    corSecundaria: payload.corSecundaria,
    preferenciaVisual: normalizeDensityPreference(payload.preferenciaVisual ?? payload.userDensityPreference),
  };
}

function normalizePreferencias(preferences = {}) {
  return {
    tema: normalizeThemePreference(preferences.tema),
    idioma: preferences.idioma ?? 'pt-BR',
    notificacoes: Boolean(preferences.notificacoes),
    corPrimaria: preferences.corPrimaria ?? null,
    corSecundaria: preferences.corSecundaria ?? null,
    preferenciaVisual: normalizeDensityPreference(preferences.preferenciaVisual),
  };
}

function normalizeAvatar(payload = {}) {
  return {
    avatarUrl: payload.avatarUrl ?? null,
  };
}

function normalizeThemePreference(value) {
  const normalized = typeof value === 'string' ? value.trim().toUpperCase() : '';
  if (normalized === 'APP_DEFAULT' || normalized === 'LIGHT' || normalized === 'DARK') {
    return normalized;
  }

  if (value === 'light') {
    return 'LIGHT';
  }

  if (value === 'dark') {
    return 'DARK';
  }

  return 'APP_DEFAULT';
}

function normalizeDensityPreference(value) {
  const normalized = typeof value === 'string' ? value.trim().toUpperCase() : '';
  if (normalized === 'APP_DEFAULT' || normalized === 'REGULAR' || normalized === 'COMPACT') {
    return normalized;
  }

  if (value === 'regular') {
    return 'REGULAR';
  }

  if (value === 'compact') {
    return 'COMPACT';
  }

  return 'APP_DEFAULT';
}
