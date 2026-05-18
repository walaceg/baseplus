import { apiClient } from '../../shared/api/apiClient.js';

export async function getUsuarios(params = {}) {
  const response = await apiClient.get('/usuarios', { params });
  return response.data.data;
}

export async function getUsuario(id) {
  const response = await apiClient.get(`/usuarios/${id}`);
  return response.data.data;
}

export async function createUsuario(payload) {
  const response = await apiClient.post('/usuarios', payload);
  return response.data.data;
}

export async function updateUsuario(id, payload) {
  const response = await apiClient.put(`/usuarios/${id}`, payload);
  return response.data.data;
}

export async function resetUsuarioSenha(id, payload) {
  const response = await apiClient.post(`/usuarios/${id}/resetar-senha`, payload);
  return response.data;
}

export async function deleteUsuario(id) {
  await apiClient.delete(`/usuarios/${id}`);
}
