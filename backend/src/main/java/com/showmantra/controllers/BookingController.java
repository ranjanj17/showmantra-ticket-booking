package com.showmantra.controllers;

import com.showmantra.dtos.BookingHistoryResponse;
import com.showmantra.dtos.BookingResponse;
import com.showmantra.dtos.SeatLockRequest;
import com.showmantra.services.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/lock")
    public ResponseEntity<BookingResponse> lockSeats(
            @RequestBody SeatLockRequest request,
            Principal principal) {
        
        // The principal object is injected by Spring Security and holds the identity of the authenticated user.
        // In our JwtAuthenticationFilter, we set the principal's name to the user's UUID string.
        UUID userId = UUID.fromString(principal.getName());
        
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

    @GetMapping("/history")
    public ResponseEntity<List<BookingHistoryResponse>> getBookingHistory(
            Principal principal) {
                
        // Extract the user ID from the securely authenticated token context
        UUID userId = UUID.fromString(principal.getName());
        
        var history = bookingService.getUserBookingHistory(userId);
        if (history.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(history);
    }
}
