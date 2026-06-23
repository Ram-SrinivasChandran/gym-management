import { apiClient } from '../../api/client';

export async function searchMembers({ search, branchId, page = 0, size = 20 } = {}) {
  const response = await apiClient.get('/members', {
    params: { search: search || undefined, branchId: branchId || undefined, page, size },
  });
  return response.data;
}

export async function getMember(memberId) {
  const response = await apiClient.get(`/members/${memberId}`);
  return response.data;
}

export async function createMember(payload) {
  const response = await apiClient.post('/members', payload);
  return response.data;
}

export async function updateMember(memberId, payload) {
  const response = await apiClient.patch(`/members/${memberId}`, payload);
  return response.data;
}

export async function deactivateMember(memberId) {
  const response = await apiClient.delete(`/members/${memberId}`);
  return response.data;
}
