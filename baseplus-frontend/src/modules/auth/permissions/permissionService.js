import { apiClient } from '../../../shared/api/apiClient.js';

export async function getPermissions(params = {}) {
  const response = await apiClient.get('/permissions', { params });
  return response.data.data;
}

export async function getPermission(id) {
  const response = await apiClient.get(`/permissions/${id}`);
  return response.data.data;
}

export async function createPermission(payload) {
  const response = await apiClient.post('/permissions', payload);
  return response.data.data;
}

export async function updatePermission(id, payload) {
  const response = await apiClient.put(`/permissions/${id}`, payload);
  return response.data.data;
}

export async function deletePermission(id) {
  await apiClient.delete(`/permissions/${id}`);
}
