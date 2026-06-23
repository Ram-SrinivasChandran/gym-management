import { apiClient } from '../../api/client';

export async function login(email, password) {
  const response = await apiClient.post('/auth/login', { email, password });
  return response.data;
}

export async function logout(refreshToken) {
  await apiClient.post('/auth/logout', { refreshToken });
}
