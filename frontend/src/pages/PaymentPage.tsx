import React, { useState } from 'react';
import { useParams, useLocation, useNavigate } from 'react-router-dom';
import { Smartphone, CreditCard, Landmark, Gift, CheckCircle, Loader2 } from 'lucide-react';
import { confirmBooking } from '../services/bookingService';
import { useBookingStore } from '../store/useBookingStore';

export const PaymentPage = () => {
  const { bookingId } = useParams<{ bookingId: string }>();
  const location = useLocation();
  const navigate = useNavigate();
  const [paymentStatus, setPaymentStatus] = useState<'idle' | 'processing' | 'success' | 'error'>('idle');

  const { movieTitle, selectedSeatsData, totalPrice } = location.state || {};

  if (!bookingId || !movieTitle) {
    return <div className="min-h-[calc(100vh-64px)] bg-[#121212] flex items-center justify-center text-white">Invalid Payment Session</div>;
  }

  const handlePay = async () => {
    setPaymentStatus('processing');
    try {
      // Simulate real-world payment gateway delay
      await new Promise((resolve) => setTimeout(resolve, 2500));

      await confirmBooking(bookingId);
      
      // Clear the current booking ID from the store since it is now confirmed
      useBookingStore.getState().setCurrentBookingId(null);
      
      setPaymentStatus('success');
      
      // Navigate to bookings after showing success for a brief moment
      setTimeout(() => {
        navigate('/bookings');
      }, 2000);
    } catch (error) {
      setPaymentStatus('error');
      setTimeout(() => setPaymentStatus('idle'), 3000);
    }
  };

  return (
    <div className="bg-[#121212] min-h-[calc(100vh-64px)] flex items-center justify-center p-4">
      {/* Container */}
      <div className="w-full max-w-5xl rounded-xl shadow-2xl flex flex-col md:flex-row overflow-hidden border border-gray-800">
        
        {/* Left Column: Payment Methods */}
        <div className="bg-[#5c2424] w-full md:w-1/3 p-6 flex flex-col gap-4">
          <h2 className="text-white font-bold mb-4">Choose Payment Method</h2>
          
          <button className="bg-white text-[#5c2424] p-4 rounded-lg flex items-center gap-4 font-bold shadow-md">
            <Smartphone size={20} /> Pay by any UPI App
          </button>
          
          <button className="bg-[#783737] text-white p-4 rounded-lg flex items-center gap-4 font-medium hover:bg-[#864040] transition-colors">
            <CreditCard size={20} className="text-yellow-400" /> Debit/ Credit Card
          </button>
          
          <button className="bg-[#783737] text-white p-4 rounded-lg flex items-center gap-4 font-medium hover:bg-[#864040] transition-colors">
            <Landmark size={20} className="text-blue-300" /> Net Banking
          </button>

          <button className="bg-[#783737] text-white p-4 rounded-lg flex items-center gap-4 font-medium hover:bg-[#864040] transition-colors">
            <Gift size={20} className="text-orange-400" /> Gift Voucher
          </button>
        </div>

        {/* Middle Column: UPI Details */}
        <div className="bg-[#783737] w-full md:w-1/3 p-6 flex flex-col">
          <h2 className="text-white font-bold text-xl mb-2">UPI Payment</h2>
          <p className="text-gray-300 text-sm mb-6">Scan to pay securely via Razorpay</p>
          
          <div className="bg-white p-4 rounded-lg mb-6 self-center">
            <img 
              src={`https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=upi://pay?pa=merchant@upi&pn=BookMySeat&am=${totalPrice}&cu=INR`} 
              alt="UPI QR Code" 
              className="w-48 h-48"
            />
          </div>

          <button 
            onClick={handlePay}
            disabled={paymentStatus !== 'idle'}
            className="w-full bg-[#ff0000] hover:bg-[#cc0000] text-white font-bold py-4 rounded-lg shadow-lg transition-colors mt-auto disabled:opacity-50 cursor-pointer"
          >
            {paymentStatus === 'processing' ? 'PROCESSING...' : `PAY ₹${totalPrice}`}
          </button>
        </div>

        {/* Right Column: Order Summary */}
        <div className="bg-[#e2e2e2] w-full md:w-1/3 p-6 flex flex-col text-gray-800">
          <h2 className="font-bold text-xl mb-6 text-black">Summary</h2>
          
          <div className="bg-[#f0f0f0] p-4 rounded-lg border-l-4 border-red-500 mb-8">
            <div className="flex justify-between items-start mb-2">
              <h3 className="font-bold text-black">{movieTitle}</h3>
              <span onClick={() => navigate(-1)} className="text-red-500 text-xs font-semibold cursor-pointer hover:underline">Edit Seats</span>
            </div>
            <p className="text-sm text-gray-600 mb-1">Today | 7:00 PM</p>
            <p className="text-sm text-gray-600">
              Seats: {selectedSeatsData?.map((seat: any) => `${seat.row}${seat.number}`).join(', ')}
            </p>
          </div>

          <div className="flex justify-between items-center mb-4 text-gray-600 text-sm">
            <span>Total Price</span>
            <span>₹{totalPrice.toFixed(2)}</span>
          </div>

          <div className="flex justify-between items-center mb-8 font-bold text-black border-t border-gray-300 pt-4">
            <span>Order Total</span>
            <span>₹{totalPrice.toFixed(2)}</span>
          </div>

          <div className="mt-auto bg-[#dcdcdc] rounded-lg p-4 border border-gray-300 border-dashed text-center font-bold text-black flex justify-between">
            <span>Amount Payable:</span> <span>₹{totalPrice} /-</span>
          </div>
        </div>

      </div>

      {/* Overlay for processing/success/error */}
      {paymentStatus !== 'idle' && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm transition-opacity">
          <div className="bg-white p-8 rounded-2xl shadow-2xl max-w-sm w-full flex flex-col items-center text-center transform transition-all scale-100">
            {paymentStatus === 'processing' && (
              <>
                <Loader2 className="w-16 h-16 text-blue-500 animate-spin mb-4" />
                <h3 className="text-xl font-bold text-gray-900 mb-2">Processing Payment</h3>
                <p className="text-sm text-gray-500">Please do not close this window or press back.</p>
              </>
            )}
            
            {paymentStatus === 'success' && (
              <>
                <CheckCircle className="w-16 h-16 text-green-500 mb-4 animate-[bounce_1s_ease-in-out]" />
                <h3 className="text-xl font-bold text-gray-900 mb-2">Payment Successful!</h3>
                <p className="text-sm text-gray-500">Your tickets have been booked.</p>
                <p className="text-xs text-gray-400 mt-4">Redirecting...</p>
              </>
            )}

            {paymentStatus === 'error' && (
              <>
                <div className="w-16 h-16 rounded-full bg-red-100 flex items-center justify-center mb-4">
                  <span className="text-red-500 text-3xl font-bold">!</span>
                </div>
                <h3 className="text-xl font-bold text-gray-900 mb-2">Payment Failed</h3>
                <p className="text-sm text-gray-500">Something went wrong. Please try again.</p>
              </>
            )}
          </div>
        </div>
      )}
    </div>
  );
};
