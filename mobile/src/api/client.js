import axios from 'axios';
import { API_BASE_URL } from './config';
import { supabase } from './supabase';

export const apiClient = axios.create({
  baseURL: API_BASE_URL,
  timeout: 15000,
});

// Attach the current Supabase access token to every request. getSession() returns the
// cached session (Supabase refreshes it in the background when autoRefreshToken is on).
apiClient.interceptors.request.use(async (config) => {
  const { data } = await supabase.auth.getSession();
  const token = data.session?.access_token;
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    const status = error.response?.status;

    if (status !== 401 || originalRequest._retry) {
      return Promise.reject(error);
    }
    originalRequest._retry = true;

    // Force a token refresh through Supabase, then retry once. If refresh fails, sign out.
    const { data, error: refreshError } = await supabase.auth.refreshSession();
    const newAccessToken = data?.session?.access_token;
    if (refreshError || !newAccessToken) {
      await supabase.auth.signOut();
      return Promise.reject(refreshError ?? error);
    }

    originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;
    return apiClient(originalRequest);
  }
);
