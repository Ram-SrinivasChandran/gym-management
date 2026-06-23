import { create } from 'zustand';

export const useToastStore = create((set) => ({
  message: null,
  showToast: (message) => set({ message }),
  hideToast: () => set({ message: null }),
}));
