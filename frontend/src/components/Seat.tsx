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
    if (isSelected || seat.status === 'SELECTED') return 'bg-green-500 text-black border-green-500';
    switch (seat.status) {
      case 'AVAILABLE':
        return 'bg-transparent text-green-500 border-green-500 hover:bg-green-500/20';
      case 'LOCKED':
        return 'bg-yellow-600 text-black border-yellow-600 cursor-not-allowed';
      case 'BOOKED':
        return 'bg-gray-500 text-gray-800 border-gray-500 cursor-not-allowed opacity-50';
      default:
        return 'bg-gray-500 text-gray-300 border-gray-500';
    }
  };

  return (
    <button
      onClick={handleClick}
      disabled={seat.status === 'BOOKED' || seat.status === 'LOCKED'}
      className={`w-8 h-8 rounded border flex items-center justify-center text-xs font-medium transition-colors duration-200 ${getStatusColor()}`}
      title={`Row ${seat.row} - Seat ${seat.number}`}
    >
      {seat.number}
    </button>
  );
};
