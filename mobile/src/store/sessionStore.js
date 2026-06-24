import { create } from 'zustand';
import { supabase } from '../api/supabase';

// Auth is owned by Supabase. The store mirrors the current Supabase access token so the
// navigator can gate on it; Supabase persists/refreshes the session itself (AsyncStorage).
export const useSessionStore = create((set, get) => ({
  accessToken: null,
  hydrated: false,

  hydrate: async () => {
    const { data } = await supabase.auth.getSession();
    set({ accessToken: data.session?.access_token ?? null, hydrated: true });

    // Keep the token in sync on sign-in, sign-out, and silent token refreshes.
    supabase.auth.onAuthStateChange((_event, session) => {
      set({ accessToken: session?.access_token ?? null });
    });
  },

  isAuthenticated: () => Boolean(get().accessToken),
}));
