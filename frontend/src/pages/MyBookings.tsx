import React, { useEffect, useState } from 'react';
import { useAuthStore } from '../store/useAuthStore';
import { getBookingHistory, cancelBooking, BookingHistoryData } from '../services/bookingService';
import toast from 'react-hot-toast';
import { Ticket, Calendar, Clock, MapPin, XCircle, CheckCircle2 } from 'lucide-react';

export const MyBookings = () => {
  const { user, setAuthModalOpen } = useAuthStore();
  const [bookings, setBookings] = useState<BookingHistoryData[]>([]);
  const [loading, setLoading] = useState(true);

  const loadBookings = async () => {
    if (!user) {
      setLoading(false);
      return;
    }
    try {
      setLoading(true);
      const data = await getBookingHistory();
      // Sort by showtime descending (latest shows first)
      data.sort((a, b) => new Date(b.showTime).getTime() - new Date(a.showTime).getTime());
      setBookings(data);
    } catch (error) {
      toast.error('Failed to load booking history');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadBookings();
  }, [user]);

  if (!user) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-24 text-center">
        <Ticket size={64} className="mx-auto text-gray-300 mb-6" />
        <h1 className="text-3xl font-bold text-gray-900 mb-4">Your Bookings</h1>
        <p className="text-gray-500 mb-8 max-w-md mx-auto">Please log in to view your upcoming movie plans and past booking history.</p>
        <button 
          onClick={() => setAuthModalOpen(true)}
          className="bg-red-500 hover:bg-red-600 text-white font-semibold px-8 py-3 rounded-lg transition-colors shadow-sm"
        >
          Sign In
        </button>
      </div>
    );
  }

  const handleCancel = async (bookingId: string) => {
    if (window.confirm('Are you sure you want to cancel this booking? This action cannot be undone.')) {
      try {
        await cancelBooking(bookingId);
        toast.success('Booking cancelled successfully');
        loadBookings();
      } catch (error: any) {
        toast.error(error.response?.data?.message || 'Failed to cancel booking');
      }
    }
  };

  const formatDate = (dateString: string) => {
    const options: Intl.DateTimeFormatOptions = { weekday: 'short', month: 'short', day: 'numeric', year: 'numeric' };
    return new Date(dateString).toLocaleDateString(undefined, options);
  };

  const formatTime = (dateString: string) => {
    return new Date(dateString).toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit' });
  };

  return (
    <div className="min-h-screen bg-gray-50 py-10">
      <div className="max-w-4xl mx-auto px-4">
        <h1 className="text-3xl font-bold text-gray-900 mb-8">Booking History</h1>
        
        {loading ? (
          <div className="flex justify-center py-12">
            <div className="animate-spin rounded-full h-12 w-12 border-4 border-gray-200 border-t-red-500"></div>
          </div>
        ) : bookings.length === 0 ? (
          <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-12 text-center">
            <Ticket size={48} className="mx-auto text-gray-300 mb-4" />
            <h3 className="text-xl font-semibold text-gray-900 mb-2">No bookings found</h3>
            <p className="text-gray-500">Looks like you haven't booked any tickets yet.</p>
          </div>
        ) : (
          <div className="space-y-6">
            {bookings.map((booking) => {
              const showDate = new Date(booking.showTime);
              const now = new Date();
              const isFutureShow = showDate > now;
              const isCancelled = booking.status === 'CANCELLED';
              const isCancellable = isFutureShow && !isCancelled;

              return (
                <div key={booking.bookingId} className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden flex flex-col md:flex-row transition-shadow hover:shadow-md">
                  <div className="p-6 flex-1">
                    <div className="flex justify-between items-start mb-4">
                      <div>
                        <h2 className="text-2xl font-bold text-gray-900 mb-1">{booking.movieTitle}</h2>
                        <div className="flex items-center text-gray-500 text-sm gap-4">
                          <span className="flex items-center gap-1"><MapPin size={14} /> {booking.theaterName} ({booking.screenName})</span>
                        </div>
                      </div>
                      <div className={`px-3 py-1 rounded-full text-xs font-semibold ${isCancelled ? 'bg-red-50 text-red-600 border border-red-100' : 'bg-green-50 text-green-600 border border-green-100'}`}>
                        {booking.status}
                      </div>
                    </div>
                    
                    <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mt-6">
                      <div className="bg-gray-50 p-3 rounded-lg">
                        <div className="flex items-center gap-2 text-gray-500 text-xs font-medium uppercase mb-1">
                          <Calendar size={14} /> Date
                        </div>
                        <div className="font-semibold text-gray-900">{formatDate(booking.showTime)}</div>
                      </div>
                      <div className="bg-gray-50 p-3 rounded-lg">
                        <div className="flex items-center gap-2 text-gray-500 text-xs font-medium uppercase mb-1">
                          <Clock size={14} /> Time
                        </div>
                        <div className="font-semibold text-gray-900">{formatTime(booking.showTime)}</div>
                      </div>
                      <div className="bg-gray-50 p-3 rounded-lg">
                        <div className="flex items-center gap-2 text-gray-500 text-xs font-medium uppercase mb-1">
                          <Ticket size={14} /> Seats ({booking.bookedSeats.length})
                        </div>
                        <div className="font-semibold text-gray-900 truncate" title={booking.bookedSeats.join(', ')}>
                          {booking.bookedSeats.join(', ')}
                        </div>
                      </div>
                      <div className="bg-gray-50 p-3 rounded-lg">
                        <div className="flex items-center gap-2 text-gray-500 text-xs font-medium uppercase mb-1">
                          Amount
                        </div>
                        <div className="font-semibold text-gray-900">₹{booking.totalAmount.toFixed(2)}</div>
                      </div>
                    </div>
                  </div>
                  
                  {/* Action Section */}
                  <div className="bg-gray-50 p-6 md:w-64 border-t md:border-t-0 md:border-l border-gray-100 flex flex-col justify-center items-center text-center">
                    {isCancelled ? (
                      <div className="text-red-500 flex flex-col items-center gap-2">
                        <XCircle size={32} />
                        <span className="font-semibold text-sm">Booking Cancelled</span>
                        <span className="text-xs text-gray-500 mt-1">Refunds take 3-5 business days</span>
                      </div>
                    ) : !isFutureShow ? (
                      <div className="text-green-500 flex flex-col items-center gap-2">
                        <CheckCircle2 size={32} />
                        <span className="font-semibold text-sm">Show Completed</span>
                      </div>
                    ) : (
                      <div className="w-full flex flex-col items-center">
                        <div className="w-16 h-16 bg-white border-2 border-dashed border-gray-300 rounded-lg flex items-center justify-center mb-4">
                          <Ticket className="text-gray-400" />
                        </div>
                        <p className="text-xs text-gray-500 mb-4">Present this ticket at the counter</p>
                        {isCancellable && (
                          <button 
                            onClick={() => handleCancel(booking.bookingId)}
                            className="w-full text-red-500 hover:text-red-700 hover:bg-red-50 font-semibold py-2 rounded-lg text-sm transition-colors border border-transparent hover:border-red-100"
                          >
                            Cancel Booking
                          </button>
                        )}
                      </div>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
};
