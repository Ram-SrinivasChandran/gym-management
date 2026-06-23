import { apiClient } from '../../api/client';

export async function listBranches() {
  const response = await apiClient.get('/branches');
  return response.data;
}
