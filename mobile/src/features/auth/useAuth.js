import { useMutation } from '@tanstack/react-query';
import { useSessionStore } from '../../store/sessionStore';
import { login as loginRequest, logout as logoutRequest } from './api';

export function useLogin() {
  const setSession = useSessionStore((state) => state.setSession);

  return useMutation({
    mutationFn: ({ email, password }) => loginRequest(email, password),
    onSuccess: (data) => setSession({ accessToken: data.accessToken, refreshToken: data.refreshToken }),
  });
}

export function useLogout() {
  const refreshToken = useSessionStore((state) => state.refreshToken);
  const clearSession = useSessionStore((state) => state.clearSession);

  return useMutation({
    mutationFn: () => logoutRequest(refreshToken),
    onSettled: () => clearSession(),
  });
}
