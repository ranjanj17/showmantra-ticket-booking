import { api } from './api';

export interface SeatData {
  id: string;
  status: 'AVAILABLE' | 'LOCKED' | 'BOOKED' | 'SELECTED';
  row: string;
  number: number;
  seatClass: string;
  price: number;
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
  const seats = response.data.seats || [];
  return seats.map((s: any) => ({
    id: s.seatId.toString(),
    status: s.status,
    row: s.rowNumber,
    number: s.seatNumber,
    seatClass: s.seatClass,
    price: s.price
  }));
};

export const lockSeats = async (showId: string, seatIds: string[]): Promise<{ bookingId: string }> => {
  const response = await api.post(`/bookings/lock`, { showId, seatIds });
  return response.data;
};

export const confirmBooking = async (bookingId: string): Promise<void> => {
  await api.post(`/bookings/${bookingId}/confirm`);
};

export const getBookingHistory = async (): Promise<BookingHistoryData[]> => {
  const response = await api.get('/bookings/history');
  return response.data || [];
};

export const cancelBooking = async (bookingId: string): Promise<void> => {
  await api.post(`/bookings/${bookingId}/cancel`);
};
