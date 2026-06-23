import { apiClient } from '../../api/client';

export async function listPlans() {
  const response = await apiClient.get('/membership-plans');
  return response.data;
}

export async function getMembershipHistory(memberId) {
  const response = await apiClient.get(`/memberships/member/${memberId}/history`);
  return response.data;
}

export async function createMembership(payload) {
  const response = await apiClient.post('/memberships', payload);
  return response.data;
}

export async function renewMembership(membershipId, newPlanId) {
  const response = await apiClient.post(`/memberships/${membershipId}/renew`, null, {
    params: { newPlanId },
  });
  return response.data;
}
