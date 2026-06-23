import Constants from 'expo-constants';

// Override at build/run time via app.json `expo.extra.apiBaseUrl` or EXPO_PUBLIC_API_BASE_URL.
const fallback = 'http://localhost:8080/api/v1';

export const API_BASE_URL =
  process.env.EXPO_PUBLIC_API_BASE_URL ??
  Constants.expoConfig?.extra?.apiBaseUrl ??
  fallback;
