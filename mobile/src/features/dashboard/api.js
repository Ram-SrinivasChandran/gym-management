import { apiClient } from '../../api/client';

export async function fetchDashboardSummary() {
  const response = await apiClient.get('/dashboard/summary');
  return response.data;
}
