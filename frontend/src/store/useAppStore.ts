import { create } from 'zustand';

interface AppState {
  city: string;
  setCity: (city: string) => void;
}

export const useAppStore = create<AppState>((set) => ({
  city: 'Bengaluru',
  setCity: (city) => set({ city }),
}));
