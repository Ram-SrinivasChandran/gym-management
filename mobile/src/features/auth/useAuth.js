import { useMutation } from '@tanstack/react-query';
import { login as loginRequest, logout as logoutRequest } from './api';

// On success, Supabase emits an auth-state change that updates the session store, which
// flips the navigator — so these mutations don't need to set session state themselves.
export function useLogin() {
  return useMutation({
    mutationFn: ({ email, password }) => loginRequest(email, password),
  });
}

export function useLogout() {
  return useMutation({
    mutationFn: () => logoutRequest(),
  });
}
