jest.mock('@react-native-async-storage/async-storage', () =>
  require('@react-native-async-storage/async-storage/jest/async-storage-mock')
);

jest.mock('expo-secure-store', () => {
  let store = {};
  return {
    getItemAsync: jest.fn((key) => Promise.resolve(store[key] ?? null)),
    setItemAsync: jest.fn((key, value) => {
      store[key] = value;
      return Promise.resolve();
    }),
    deleteItemAsync: jest.fn((key) => {
      delete store[key];
      return Promise.resolve();
    }),
    __reset: () => {
      store = {};
    },
  };
});

import * as SecureStore from 'expo-secure-store';
import { useSessionStore } from '../src/store/sessionStore';

describe('sessionStore', () => {
  beforeEach(() => {
    SecureStore.__reset();
    useSessionStore.setState({ accessToken: null, refreshToken: null, user: null, hydrated: false });
  });

  it('starts unauthenticated', () => {
    expect(useSessionStore.getState().isAuthenticated()).toBe(false);
  });

  it('setSession persists tokens to secure storage and updates state', async () => {
    await useSessionStore.getState().setSession({ accessToken: 'access-1', refreshToken: 'refresh-1' });

    expect(useSessionStore.getState().accessToken).toBe('access-1');
    expect(useSessionStore.getState().isAuthenticated()).toBe(true);
    expect(SecureStore.setItemAsync).toHaveBeenCalledWith('gym.accessToken', 'access-1');
    expect(SecureStore.setItemAsync).toHaveBeenCalledWith('gym.refreshToken', 'refresh-1');
  });

  it('hydrate reads persisted tokens back into state', async () => {
    await useSessionStore.getState().setSession({ accessToken: 'access-1', refreshToken: 'refresh-1' });
    useSessionStore.setState({ accessToken: null, refreshToken: null, hydrated: false });

    await useSessionStore.getState().hydrate();

    expect(useSessionStore.getState().accessToken).toBe('access-1');
    expect(useSessionStore.getState().refreshToken).toBe('refresh-1');
    expect(useSessionStore.getState().hydrated).toBe(true);
  });

  it('setAccessToken updates only the access token', async () => {
    await useSessionStore.getState().setSession({ accessToken: 'access-1', refreshToken: 'refresh-1' });
    await useSessionStore.getState().setAccessToken('access-2');

    expect(useSessionStore.getState().accessToken).toBe('access-2');
    expect(useSessionStore.getState().refreshToken).toBe('refresh-1');
  });

  it('clearSession removes tokens from storage and state', async () => {
    await useSessionStore.getState().setSession({ accessToken: 'access-1', refreshToken: 'refresh-1' });
    await useSessionStore.getState().clearSession();

    expect(useSessionStore.getState().accessToken).toBeNull();
    expect(useSessionStore.getState().refreshToken).toBeNull();
    expect(useSessionStore.getState().isAuthenticated()).toBe(false);
    expect(SecureStore.deleteItemAsync).toHaveBeenCalledWith('gym.accessToken');
    expect(SecureStore.deleteItemAsync).toHaveBeenCalledWith('gym.refreshToken');
  });
});
