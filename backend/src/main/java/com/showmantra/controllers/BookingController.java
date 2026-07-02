package com.showmantra.controllers;

import com.showmantra.dtos.BookingResponse;
import com.showmantra.dtos.SeatLockRequest;
import com.showmantra.services.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/lock")
    public ResponseEntity<BookingResponse> lockSeats(
            @RequestBody SeatLockRequest request,
            @RequestHeader("X-User-Id") UUID userId) {
        BookingResponse response = bookingService.lockSeats(request, userId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{bookingId}/confirm")
    public ResponseEntity<String> confirmBooking(@PathVariable UUID bookingId) {
        // In a real application, this would verify a payment intent from a payment gateway.
        bookingService.confirmBooking(bookingId);
        return ResponseEntity.ok("Booking confirmed successfully.");
    }

    @PostMapping("/{bookingId}/cancel")
    public ResponseEntity<String> cancelBooking(@PathVariable UUID bookingId) {
        bookingService.cancelBooking(bookingId);
        return ResponseEntity.ok("Booking cancelled successfully.");
    }
}
