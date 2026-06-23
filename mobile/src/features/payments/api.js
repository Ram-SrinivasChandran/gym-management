import { apiClient } from '../../api/client';

export async function recordPayment(payload) {
  const response = await apiClient.post('/payments', payload);
  return response.data;
}

export async function getPaymentHistory(membershipId) {
  const response = await apiClient.get(`/payments/membership/${membershipId}/history`);
  return response.data;
}

export async function getDueStatus(membershipId) {
  const response = await apiClient.get(`/payments/membership/${membershipId}/due`);
  return response.data;
}
