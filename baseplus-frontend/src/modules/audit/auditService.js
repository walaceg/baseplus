import { apiClient } from '../../shared/api/apiClient.js';

export async function getAuditLogs(params = {}) {
  const response = await apiClient.get('/audit-logs', { params });
  return response.data.data;
}
