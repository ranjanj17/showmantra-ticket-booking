import React from 'react';
import { SeatData } from '../services/bookingService';

interface SeatProps {
  seat: SeatData;
  isSelected: boolean;
  onClick: (seat: SeatData) => void;
}

export const Seat: React.FC<SeatProps> = ({ seat, isSelected, onClick }) => {
  const handleClick = () => {
    if (seat.status === 'AVAILABLE' || seat.status === 'SELECTED') {
      onClick(seat);
    }
  };

  const getStatusColor = () => {
    if (isSelected || seat.status === 'SELECTED') return 'bg-blue-500 hover:bg-blue-600 text-white';
    switch (seat.status) {
      case 'AVAILABLE':
        return 'bg-gray-200 hover:bg-green-400 text-gray-700';
      case 'LOCKED':
        return 'bg-yellow-400 text-white cursor-not-allowed';
      case 'BOOKED':
        return 'bg-red-500 text-white cursor-not-allowed';
      default:
        return 'bg-gray-200 text-gray-700';
    }
  };

  return (
    <button
      onClick={handleClick}
      disabled={seat.status === 'BOOKED' || seat.status === 'LOCKED'}
      className={`w-10 h-10 rounded-t-lg border-b-4 border-gray-400 font-semibold text-xs flex items-center justify-center transition-colors duration-200 ${getStatusColor()}`}
      title={`Row ${seat.row} - Seat ${seat.number}`}
    >
      {seat.number}
    </button>
  );
};
