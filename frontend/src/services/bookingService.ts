import axios from 'axios';

const API_BASE_URL = '/api'; // Base URL for the backend API

export interface SeatData {
  id: string;
  status: 'AVAILABLE' | 'LOCKED' | 'BOOKED' | 'SELECTED';
  row: string;
  number: number;
}

export const getShowSeats = async (showId: string): Promise<SeatData[]> => {
  const response = await axios.get(`${API_BASE_URL}/shows/${showId}/seats`);
  return response.data;
};

export const lockSeat = async (showId: string, seatId: string): Promise<void> => {
  await axios.post(`${API_BASE_URL}/bookings/lock`, { showId, seatId });
};
