import { useEffect, useState } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import toast from 'react-hot-toast';
import { SeatMatrix } from '../components/SeatMatrix';
import { useBookingStore } from '../store/useBookingStore';
import { useAuthStore } from '../store/useAuthStore';
import { lockSeats, getShowSeats, cancelBooking, SeatData } from '../services/bookingService';

export const BookingPage = () => {
  const { showId } = useParams<{ showId: string }>();
  const navigate = useNavigate();
  const location = useLocation();
  const movieTitle = location.state?.movieTitle || 'MOVIE';
  
  const { user, setAuthModalOpen } = useAuthStore();
  const { selectedSeatIds } = useBookingStore();
  const [isProcessing, setIsProcessing] = useState(false);
  const [seats, setSeats] = useState<SeatData[]>([]);
  const [loading, setLoading] = useState(true);

  // Handle orphaned bookings when navigating back to edit seats
  useEffect(() => {
    const handleOrphanedBooking = async () => {
      const { currentBookingId, setCurrentBookingId } = useBookingStore.getState();
      
      if (currentBookingId) {
        try {
          await cancelBooking(currentBookingId);
        } catch (e) {
          console.error('Failed to cancel orphaned booking', e);
        }
        setCurrentBookingId(null);
      } else {
        // If we didn't just come back from a booking, clear previous selections
        useBookingStore.setState({ selectedSeatIds: [] });
      }
    };
    handleOrphanedBooking();
    
    // We intentionally do NOT clear selections on unmount, so they are preserved
    // when navigating to the PaymentPage or returning back.
  }, [showId]);

  // Fetch seats
  useEffect(() => {
    const fetchSeats = async () => {
      if (!showId) return;
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

  // Calculate dynamic price based on selected seats
  const selectedSeatsData = seats.filter(seat => selectedSeatIds.includes(seat.id));
  const totalPrice = selectedSeatsData.reduce((sum, seat) => sum + (seat.price || 0), 0);

  const handleProceedToPay = async () => {
    if (!user) {
      toast.error('Please log in to proceed with booking.');
      setAuthModalOpen(true);
      return;
    }

    if (selectedSeatIds.length === 0) {
      toast.error('Please select at least one seat to proceed.');
      return;
    }
    
    if (!showId) return;

    setIsProcessing(true);
    try {
      // 1. Lock the seats
      const { bookingId } = await lockSeats(showId, selectedSeatIds);
      
      // Save it to store so we can cancel it if they navigate back
      useBookingStore.getState().setCurrentBookingId(bookingId);
      
      // 2. Redirect to Payment Page
      navigate(`/payment/${bookingId}`, {
        state: {
          movieTitle,
          selectedSeatsData,
          totalPrice
        }
      });
    } catch (error: any) {
      if (error.response && error.response.status === 409) {
        toast.error('One or more selected seats are already booked. Please choose different seats.');
      } else {
        toast.error('Failed to reserve tickets. Please try again.');
      }
    } finally {
      setIsProcessing(false);
    }
  };

  if (loading) {
    return <div className="min-h-screen bg-[#121212] flex items-center justify-center text-white">Loading seats...</div>;
  }

  return (
    <div className="bg-[#121212] flex flex-col font-sans w-full h-[calc(100vh-64px)] overflow-hidden">
      {/* Header */}
      <div className="bg-[#121212] p-4 text-center pb-2 shrink-0">
        <h1 className="text-white text-lg font-medium">
          Now Booking: <span className="text-red-500 font-bold uppercase">{movieTitle}</span>
        </h1>
      </div>

      {/* Main Content Card */}
      <div className="flex-1 p-4 sm:p-6 min-h-0 overflow-hidden">
        <div className="bg-[#1f1f1f] rounded-xl shadow-2xl border border-gray-800 w-full max-w-5xl h-full mx-auto flex flex-col p-6 overflow-hidden">
          
          <SeatMatrix seats={seats} />
          
          {/* Selected Seats Info inside the card */}
          <div className="mt-4 pt-4 shrink-0 flex flex-row justify-between items-center border-t border-gray-800">
            <div className="text-white text-sm">
              <span className="text-gray-300">Selected Seats: </span>
              <span className="font-bold tracking-wider">
                {selectedSeatsData.length > 0 
                  ? selectedSeatsData.map(seat => `${seat.row}${seat.number}`).join(', ')
                  : 'None'}
              </span>
            </div>
            
            <button
              onClick={handleProceedToPay}
              disabled={isProcessing || selectedSeatIds.length === 0}
              className={`px-8 min-w-[250px] py-3 rounded-lg font-bold text-lg tracking-wide transition-all ${
                selectedSeatIds.length > 0 && !isProcessing
                  ? 'bg-[#ff0000] hover:bg-[#cc0000] text-white shadow-lg'
                  : 'bg-[#333] text-gray-500 cursor-not-allowed'
              }`}
            >
              {isProcessing ? 'PROCESSING...' : `PROCEED TO PAY (₹${totalPrice})`}
            </button>
          </div>

        </div>
      </div>
    </div>
  );
};
