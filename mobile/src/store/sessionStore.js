import { create } from 'zustand';
import { deleteItemAsync, getItemAsync, setItemAsync } from './secureStorage';

const ACCESS_TOKEN_KEY = 'gym.accessToken';
const REFRESH_TOKEN_KEY = 'gym.refreshToken';

export const useSessionStore = create((set, get) => ({
  accessToken: null,
  refreshToken: null,
  user: null,
  hydrated: false,

  hydrate: async () => {
    const [accessToken, refreshToken] = await Promise.all([
      getItemAsync(ACCESS_TOKEN_KEY),
      getItemAsync(REFRESH_TOKEN_KEY),
    ]);
    set({ accessToken, refreshToken, hydrated: true });
  },

  setSession: async ({ accessToken, refreshToken, user }) => {
    await Promise.all([
      setItemAsync(ACCESS_TOKEN_KEY, accessToken),
      setItemAsync(REFRESH_TOKEN_KEY, refreshToken),
    ]);
    set({ accessToken, refreshToken, user: user ?? get().user });
  },

  setAccessToken: async (accessToken) => {
    await setItemAsync(ACCESS_TOKEN_KEY, accessToken);
    set({ accessToken });
  },

  clearSession: async () => {
    await Promise.all([
      deleteItemAsync(ACCESS_TOKEN_KEY),
      deleteItemAsync(REFRESH_TOKEN_KEY),
    ]);
    set({ accessToken: null, refreshToken: null, user: null });
  },

  isAuthenticated: () => Boolean(get().accessToken),
}));
