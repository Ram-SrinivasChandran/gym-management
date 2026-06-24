export const brand = {
  primary: '#DC2626',
  secondary: '#171717',
  accent: '#EF4444',
  background: '#F8FAFC',
  dark: '#0A0A0A',
};

// Text colors are intentionally fixed (not theme-driven). Cards/headers render on a
// light glass surface in BOTH light and dark mode, so text must stay dark regardless of
// the active Paper theme — otherwise dark mode flips body/title text to white and it
// disappears on the light card. `title` is the app's signature brand red for section
// headings; `primary`/`secondary`/`muted` form a readable slate ramp for body text.
export const text = {
  title: '#DC2626',
  primary: '#0F172A',
  secondary: '#475569',
  muted: '#64748B',
};

export const statusColors = {
  ACTIVE: '#10B981',
  DUE_SOON: '#F97316',
  OVERDUE: '#EF4444',
  EXPIRED: '#6B7280',
  RENEWED: '#2563EB',
  CANCELLED: '#9CA3AF',
};
