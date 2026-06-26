import { apiClient } from '../../shared/api/apiClient.js';

export async function getBranding() {
  const response = await apiClient.get('/branding');
  return response.data.data;
}

export async function getPublicBranding() {
  const response = await apiClient.get('/branding/public');
  return response.data.data;
}

export async function updateBranding(payload) {
  const response = await apiClient.put('/branding', buildPayload(payload));
  return response.data.data;
}

export async function uploadBrandingLogo(file) {
  const formData = new FormData();
  formData.append('file', file);

  const response = await apiClient.post('/branding/logo', formData);
  return response.data.data;
}

export async function uploadBrandingCompactLogo(file) {
  const formData = new FormData();
  formData.append('file', file);

  const response = await apiClient.post('/branding/compact-logo', formData);
  return response.data.data;
}

export async function uploadBrandingFavicon(file) {
  const formData = new FormData();
  formData.append('file', file);

  const response = await apiClient.post('/branding/favicon', formData);
  return response.data.data;
}

export async function uploadBrandingLoginLogo(file) {
  const formData = new FormData();
  formData.append('file', file);

  const response = await apiClient.post('/branding/login-logo', formData);
  return response.data.data;
}

export async function uploadBrandingLoginBackground(file) {
  const formData = new FormData();
  formData.append('file', file);

  const response = await apiClient.post('/branding/login-background', formData);
  return response.data.data;
}

function buildPayload(payload = {}) {
  return Object.fromEntries(
    Object.entries({
      nomePlataforma: payload.nomePlataforma,
      subtituloInstitucional: payload.subtituloInstitucional,
      tema: payload.tema,
      corPrimaria: payload.corPrimaria,
      corSecundaria: payload.corSecundaria,
      densidadeVisual: payload.densidadeVisual,
      loginBackgroundMode: payload.loginBackgroundMode,
      logoUrl: payload.logoUrl,
      compactLogoUrl: payload.compactLogoUrl,
      faviconUrl: payload.faviconUrl,
      loginLogoUrl: payload.loginLogoUrl,
      loginBackgroundUrl: payload.loginBackgroundUrl,
      whiteLabelEnabled: payload.whiteLabelEnabled,
      whiteLabelName: payload.whiteLabelName,
      whiteLabelSubtitle: payload.whiteLabelSubtitle,
    }).filter(([, value]) => value !== undefined),
  );
}
