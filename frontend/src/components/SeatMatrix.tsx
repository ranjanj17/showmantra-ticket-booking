import React, { useEffect, useState } from 'react';
import toast from 'react-hot-toast';
import { Seat } from './Seat';
import { getShowSeats, lockSeat, SeatData } from '../services/bookingService';
import { useBookingStore } from '../store/useBookingStore';

interface SeatMatrixProps {
  seats: SeatData[];
}

export const SeatMatrix: React.FC<SeatMatrixProps> = ({ seats }) => {
  const { selectedSeatIds, toggleSeatSelection } = useBookingStore();

  const handleSeatClick = (seat: SeatData) => {
    if (seat.status !== 'AVAILABLE' && seat.status !== 'SELECTED') return;
    toggleSeatSelection(seat.id);
  };

  if (!Array.isArray(seats) || seats.length === 0) {
    return (
      <div className="text-red-500 text-center py-4">
        Seat data could not be loaded or is empty.
      </div>
    );
  }

  // Group seats by seatClass
  const classMap = seats.reduce((acc, seat) => {
    if (!acc[seat.seatClass]) acc[seat.seatClass] = [];
    acc[seat.seatClass].push(seat);
    return acc;
  }, {} as Record<string, SeatData[]>);

  // Define class order (Silver, Gold, Platinum)
  const classOrder = ['SILVER', 'GOLD', 'PLATINUM'];
  const sortedClasses = Object.keys(classMap).sort((a, b) => {
    const indexA = classOrder.indexOf(a);
    const indexB = classOrder.indexOf(b);
    return (indexA !== -1 ? indexA : 99) - (indexB !== -1 ? indexB : 99);
  });

  return (
    <div className="flex flex-col flex-1 min-h-0">
      <div className="flex-1 overflow-y-auto no-scrollbar pb-4 min-h-0">
        <div className="w-full relative mb-12 shrink-0 px-4 mt-2">
          <div className="w-full h-12 bg-gradient-to-t from-transparent to-gray-600 rounded-t-[50%] opacity-50 shadow-[0_15px_15px_rgba(255,255,255,0.1)]"></div>
          <p className="absolute -bottom-4 w-full text-center text-xs text-gray-400 tracking-[0.5em]">SCREEN</p>
        </div>

        <div className="flex flex-col gap-8 min-w-max items-center mx-auto pr-4">
          {sortedClasses.map(seatClass => {
            const classSeats = classMap[seatClass];
            const price = classSeats[0]?.price || 0;
            
            // Group by row within class
            const rowMap = classSeats.reduce((acc, seat) => {
              if (!acc[seat.row]) acc[seat.row] = [];
              acc[seat.row].push(seat);
              return acc;
            }, {} as Record<string, SeatData[]>);
            
            const sortedRows = Object.keys(rowMap).sort();

            return (
              <div key={seatClass} className="flex flex-col gap-3 w-full">
                <div className="mb-2 w-full flex justify-center text-gray-400 text-sm font-medium tracking-widest uppercase border-b border-gray-800 pb-2">
                  ₹{price} {seatClass}
                </div>
                
                {sortedRows.map(rowId => {
                  const rowSeats = rowMap[rowId].sort((a, b) => a.number - b.number);
                  const midPoint = Math.ceil(rowSeats.length / 2);
                  const leftBlock = rowSeats.slice(0, midPoint);
                  const rightBlock = rowSeats.slice(midPoint);

                  return (
                    <div key={rowId} className="flex items-center justify-center gap-6">
                      <div className="w-6 text-gray-400 font-semibold text-right">{rowId}</div>
                      
                      <div className="flex gap-2">
                        {leftBlock.map(seat => (
                          <Seat 
                            key={seat.id} 
                            seat={seat} 
                            isSelected={selectedSeatIds.includes(seat.id)} 
                            onClick={handleSeatClick} 
                          />
                        ))}
                      </div>

                      <div className="w-12"></div>

                      <div className="flex gap-2">
                        {rightBlock.map(seat => (
                          <Seat 
                            key={seat.id} 
                            seat={seat} 
                            isSelected={selectedSeatIds.includes(seat.id)} 
                            onClick={handleSeatClick} 
                          />
                        ))}
                      </div>
                    </div>
                  );
                })}
              </div>
            );
          })}
        </div>
      </div>
      
      <div className="flex justify-center bg-[#2a2a2a] py-3 px-6 rounded-lg gap-8 text-sm text-gray-300 w-full shrink-0 mt-4 shadow-inner">
        <div className="flex items-center gap-2">
          <div className="w-4 h-4 bg-transparent border border-green-500 rounded-sm"></div> 
          <span>Available</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-4 h-4 bg-green-500 rounded-sm"></div> 
          <span>Selected</span>
        </div>
        <div className="flex items-center gap-2">
          <div className="w-4 h-4 bg-gray-500 rounded-sm opacity-50"></div> 
          <span>Sold</span>
        </div>
      </div>
    </div>
  );
};
