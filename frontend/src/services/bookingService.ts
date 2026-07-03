import { api } from './api';

export interface SeatData {
  id: string;
  status: 'AVAILABLE' | 'LOCKED' | 'BOOKED' | 'SELECTED';
  row: string;
  number: number;
}

export interface BookingHistoryData {
  bookingId: string;
  movieTitle: string;
  theaterName: string;
  screenName: string;
  showTime: string;
  bookedSeats: string[];
  totalAmount: number;
  status: string;
  bookedAt: string;
}

export const getShowSeats = async (showId: string): Promise<SeatData[]> => {
  const response = await api.get(`/shows/${showId}/seats`);
  return response.data;
};

export const lockSeat = async (showId: string, seatId: string): Promise<void> => {
  await api.post(`/bookings/lock`, { showId, seatId });
};

export const getBookingHistory = async (): Promise<BookingHistoryData[]> => {
  const response = await api.get('/bookings/history');
  return response.data || [];
};

export const cancelBooking = async (bookingId: string): Promise<void> => {
  await api.post(`/bookings/${bookingId}/cancel`);
};
