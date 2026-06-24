import AsyncStorage from '@react-native-async-storage/async-storage';
import { createClient } from '@supabase/supabase-js';

// Supabase project (Red Fitness, ap-south-1). The anon key is a public client key — it is
// safe to ship in the app; access is still governed by the backend + Supabase RLS.
// Override at build time via EXPO_PUBLIC_SUPABASE_URL / EXPO_PUBLIC_SUPABASE_ANON_KEY.
const SUPABASE_URL = process.env.EXPO_PUBLIC_SUPABASE_URL ?? 'https://mzijhzfibysljfpsfurp.supabase.co';
const SUPABASE_ANON_KEY =
  process.env.EXPO_PUBLIC_SUPABASE_ANON_KEY ??
  'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Im16aWpoemZpYnlzbGpmcHNmdXJwIiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODIyODU2NTMsImV4cCI6MjA5Nzg2MTY1M30.oVg179TCHvZLvgBqng-S2a2GHWwrdE3scJRIiRJxiCE';

export const supabase = createClient(SUPABASE_URL, SUPABASE_ANON_KEY, {
  auth: {
    storage: AsyncStorage,
    autoRefreshToken: true,
    persistSession: true,
    // No URL-based session detection in a native app.
    detectSessionInUrl: false,
  },
});
