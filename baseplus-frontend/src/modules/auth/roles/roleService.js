import { apiClient } from '../../../shared/api/apiClient.js';

export async function getRoles(params = {}) {
  const response = await apiClient.get('/roles', { params });
  return response.data.data;
}

export async function getRole(id) {
  const response = await apiClient.get(`/roles/${id}`);
  return response.data.data;
}

export async function createRole(payload) {
  const response = await apiClient.post('/roles', payload);
  return response.data.data;
}

export async function updateRole(id, payload) {
  const response = await apiClient.put(`/roles/${id}`, payload);
  return response.data.data;
}

export async function updateRoleStatus(id, ativo) {
  const response = await apiClient.patch(`/roles/${id}/status`, { ativo });
  return response.data.data;
}

export async function addRolePermission(id, permissionId) {
  const response = await apiClient.post(`/roles/${id}/permissions/${permissionId}`);
  return response.data.data;
}

export async function removeRolePermission(id, permissionId) {
  const response = await apiClient.delete(`/roles/${id}/permissions/${permissionId}`);
  return response.data.data;
}

export async function addRoleOrganizationScope(id, payload) {
  const response = await apiClient.post(`/roles/${id}/organization-scopes`, payload);
  return response.data.data;
}

export async function removeRoleOrganizationScope(id, scopeId) {
  const response = await apiClient.delete(`/roles/${id}/organization-scopes/${scopeId}`);
  return response.data.data;
}

export async function getRoleUsuarios(id, params = {}) {
  const response = await apiClient.get(`/roles/${id}/usuarios`, { params });
  return response.data.data;
}

export async function addRoleUsuario(id, usuarioId) {
  const response = await apiClient.post(`/roles/${id}/usuarios/${usuarioId}`);
  return response.data;
}

export async function removeRoleUsuario(id, usuarioId) {
  const response = await apiClient.delete(`/roles/${id}/usuarios/${usuarioId}`);
  return response.data;
}

export async function deleteRole(id) {
  await apiClient.delete(`/roles/${id}`);
}

export async function getPermissions(params = {}) {
  const response = await apiClient.get('/permissions', { params });
  return response.data.data;
}

export async function getOrganizationUnits(params = {}) {
  const response = await apiClient.get('/organization-units', { params });
  return response.data.data;
}
