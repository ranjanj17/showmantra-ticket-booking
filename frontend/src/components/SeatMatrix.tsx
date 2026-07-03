import React, { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { Seat } from './Seat';
import { getShowSeats, lockSeat, SeatData } from '../services/bookingService';
import { useBookingStore } from '../store/useBookingStore';

interface SeatMatrixProps {
  showId: string;
  seatsPerRow?: number;
}

export const SeatMatrix: React.FC<SeatMatrixProps> = ({ showId, seatsPerRow = 10 }) => {
  const [seats, setSeats] = useState<SeatData[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const { selectedSeatIds, toggleSeatSelection, removeSeatSelection, setLocking } = useBookingStore();

  useEffect(() => {
    const fetchSeats = async () => {
      try {
        const data = await getShowSeats(showId);
        setSeats(data);
      } catch (error) {
        toast.error('Failed to load seat layout.');
      } finally {
        setLoading(false);
      }
    };
    fetchSeats();
  }, [showId]);

  const handleSeatClick = async (seat: SeatData) => {
    if (seat.status !== 'AVAILABLE' && seat.status !== 'SELECTED') return;

    const isSelecting = !selectedSeatIds.includes(seat.id);
    
    // Optimistic UI update
    toggleSeatSelection(seat.id);

    if (isSelecting) {
      try {
        setLocking(true);
        await lockSeat(showId, seat.id);
      } catch (error: any) {
        // Revert selection on failure
        removeSeatSelection(seat.id);
        if (error.response && error.response.status === 409) {
          toast.error('This seat is already locked or booked by someone else.');
        } else {
          toast.error('Failed to lock seat. Please try again.');
        }
      } finally {
        setLocking(false);
      }
    }
  };

  if (loading) {
    return <div className="flex justify-center p-8">Loading seats...</div>;
  }

  return (
    <div className="flex flex-col items-center gap-6 p-4">
      <div className="w-full max-w-2xl bg-gray-300 h-2 rounded-full mb-8 shadow-sm">
        <p className="text-center text-xs text-gray-500 mt-2 tracking-widest">SCREEN</p>
      </div>
      
      <div 
        className="grid gap-2" 
        style={{ gridTemplateColumns: `repeat(${seatsPerRow}, minmax(0, 1fr))` }}>
        {Array.isArray(seats) ? seats.map((seat) => (
          <Seat 
            key={seat.id} 
            seat={seat} 
            isSelected={selectedSeatIds.includes(seat.id)} 
            onClick={handleSeatClick} 
          />
        )) : (
          <div className="text-red-500 col-span-full text-center py-4">
            Seat data could not be loaded. Please ensure the backend API is running and returns an array.
          </div>
        )}
      </div>
      
      <div className="flex gap-4 mt-8 text-sm">
        <div className="flex items-center gap-2"><div className="w-4 h-4 bg-gray-200 rounded"></div> Available</div>
        <div className="flex items-center gap-2"><div className="w-4 h-4 bg-blue-500 rounded"></div> Selected</div>
        <div className="flex items-center gap-2"><div className="w-4 h-4 bg-yellow-400 rounded"></div> Locked</div>
        <div className="flex items-center gap-2"><div className="w-4 h-4 bg-red-500 rounded"></div> Booked</div>
      </div>
    </div>
  );
};
