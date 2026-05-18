import { apiClient } from '../../shared/api/apiClient.js';

export async function getOrganizationUnitTypes(params = {}) {
  const response = await apiClient.get('/organization-unit-types', { params });
  return response.data.data;
}

export async function getOrganizationUnitType(id) {
  const response = await apiClient.get(`/organization-unit-types/${id}`);
  return response.data.data;
}

export async function createOrganizationUnitType(payload) {
  const response = await apiClient.post('/organization-unit-types', payload);
  return response.data.data;
}

export async function updateOrganizationUnitType(id, payload) {
  const response = await apiClient.put(`/organization-unit-types/${id}`, payload);
  return response.data.data;
}

export async function deleteOrganizationUnitType(id) {
  await apiClient.delete(`/organization-unit-types/${id}`);
}

export async function getOrganizationUnits(params = {}) {
  const response = await apiClient.get('/organization-units', { params });
  return response.data.data;
}

export async function getOrganizationUnit(id) {
  const response = await apiClient.get(`/organization-units/${id}`);
  return response.data.data;
}

export async function createOrganizationUnit(payload) {
  const response = await apiClient.post('/organization-units', payload);
  return response.data.data;
}

export async function updateOrganizationUnit(id, payload) {
  const response = await apiClient.put(`/organization-units/${id}`, payload);
  return response.data.data;
}

export async function deleteOrganizationUnit(id) {
  await apiClient.delete(`/organization-units/${id}`);
}
