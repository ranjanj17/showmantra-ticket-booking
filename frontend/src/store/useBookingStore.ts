import { create } from 'zustand';

interface BookingState {
  selectedSeatIds: string[];
  isLocking: boolean;
  currentBookingId: string | null;
  toggleSeatSelection: (seatId: string) => void;
  setLocking: (isLocking: boolean) => void;
  removeSeatSelection: (seatId: string) => void;
  setCurrentBookingId: (id: string | null) => void;
}

export const useBookingStore = create<BookingState>((set) => ({
  selectedSeatIds: [],
  isLocking: false,
  currentBookingId: null,
  toggleSeatSelection: (seatId) => set((state) => ({
    selectedSeatIds: state.selectedSeatIds.includes(seatId)
      ? state.selectedSeatIds.filter(id => id !== seatId)
      : [...state.selectedSeatIds, seatId]
  })),
  removeSeatSelection: (seatId) => set((state) => ({
    selectedSeatIds: state.selectedSeatIds.filter(id => id !== seatId)
  })),
  setLocking: (isLocking) => set({ isLocking }),
  setCurrentBookingId: (id) => set({ currentBookingId: id }),
}));
