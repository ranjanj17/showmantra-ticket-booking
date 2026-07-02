package com.showmantra.services;

import com.showmantra.dtos.PaymentRequest;
import com.showmantra.dtos.PaymentResponse;
import com.showmantra.entities.Booking;
import com.showmantra.entities.Payment;
import com.showmantra.entities.enums.BookingStatus;
import com.showmantra.entities.enums.PaymentStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import com.showmantra.repositories.BookingRepository;
import com.showmantra.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

/**
 * Handles payment processing and links successful payments to bookings.
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final BookingService bookingService;

    /**
     * Processes a payment for a given booking. If successful, it updates the booking status to CONFIRMED.
     * @param request The payment details including the booking ID and amount.
     * @return A PaymentResponse containing the outcome.
     * @throws IllegalArgumentException if booking is invalid or amount mismatches.
     * @throws ResponseStatusException if booking is not in a valid state or payment already exists.
     */
    @Transactional
    public PaymentResponse processPayment(PaymentRequest request) {
        // 1. Fetch the booking
        Booking booking = bookingRepository.findById(request.bookingId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Booking is already processed or expired. Current status: " + booking.getStatus());
        }

        // 2. Validate amount matches
        if (booking.getTotalAmount().compareTo(request.amount()) != 0) {
            throw new IllegalArgumentException("Payment amount does not match the booking total amount");
        }

        // 3. Check if a payment already exists for this booking to prevent double charging
        if (paymentRepository.findByBookingId(booking.getId()).isPresent()) {
             throw new ResponseStatusException(HttpStatus.CONFLICT, "A payment has already been initiated for this booking.");
        }

        // 4. Simulate Third-Party Payment Gateway (Stripe/Razorpay, etc.)
        boolean paymentSuccess = simulatePaymentGateway(request.paymentMethodDetails());

        // 5. Create Payment Entity
        Payment payment = Payment.builder()
                .booking(booking)
                .amount(request.amount())
                .providerReference("sim_" + UUID.randomUUID().toString().substring(0, 8)) // Simulated ref
                .status(paymentSuccess ? PaymentStatus.SUCCESS : PaymentStatus.FAILED)
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        // 6. Confirm Booking if payment succeeded
        if (paymentSuccess) {
            bookingService.confirmBooking(booking.getId());
            return new PaymentResponse(savedPayment.getId(), booking.getId(), "SUCCESS", "Payment processed successfully.");
        } else {
            return new PaymentResponse(savedPayment.getId(), booking.getId(), "FAILED", "Payment failed at the gateway.");
        }
    }

    /**
     * Dummy logic to simulate an external gateway call.
     */
    private boolean simulatePaymentGateway(String paymentDetails) {
        // We'll simulate a failure if the details string is missing or says 'fail'
        return paymentDetails != null && 
               !paymentDetails.trim().isEmpty() && 
               !paymentDetails.equalsIgnoreCase("fail");
    }
}
