import { create } from 'zustand';

interface BookingState {
  selectedSeatIds: string[];
  isLocking: boolean;
  toggleSeatSelection: (seatId: string) => void;
  setLocking: (isLocking: boolean) => void;
  removeSeatSelection: (seatId: string) => void;
}

export const useBookingStore = create<BookingState>((set) => ({
  selectedSeatIds: [],
  isLocking: false,
  toggleSeatSelection: (seatId) => set((state) => ({
    selectedSeatIds: state.selectedSeatIds.includes(seatId)
      ? state.selectedSeatIds.filter(id => id !== seatId)
      : [...state.selectedSeatIds, seatId]
  })),
  removeSeatSelection: (seatId) => set((state) => ({
    selectedSeatIds: state.selectedSeatIds.filter(id => id !== seatId)
  })),
  setLocking: (isLocking) => set({ isLocking }),
}));
