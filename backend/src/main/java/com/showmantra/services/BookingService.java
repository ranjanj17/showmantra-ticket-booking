package com.showmantra.services;

import com.showmantra.dtos.BookingResponse;
import com.showmantra.dtos.SeatLockRequest;
import com.showmantra.entities.Booking;
import com.showmantra.entities.Show;
import com.showmantra.entities.ShowSeat;
import com.showmantra.entities.User;
import com.showmantra.entities.enums.BookingStatus;
import com.showmantra.entities.enums.ShowSeatStatus;
import com.showmantra.exceptions.ConflictException;
import com.showmantra.repositories.BookingRepository;
import com.showmantra.repositories.ShowRepository;
import com.showmantra.repositories.ShowSeatRepository;
import com.showmantra.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Handles the core business logic for locking seats and creating bookings.
 */
@Service
@RequiredArgsConstructor
public class BookingService {

    private final ShowSeatRepository showSeatRepository;
    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final ShowRepository showRepository;
    private final RedisService redisService;

    /**
     * Locks the requested seats for a specific user and show, and creates a pending booking.
     * This method uses a distributed Redis lock to prevent concurrent double-booking attempts
     * for the same seats.
     *
     * @param request the seat lock request containing the show ID and requested seat IDs
     * @param userId the UUID of the user attempting to book the seats
     * @return a BookingResponse containing the pending booking details
     * @throws IllegalArgumentException if the user, show, or requested seats are invalid
     * @throws ConflictException if any of the requested seats are already locked or booked
     */
    @Transactional
    public BookingResponse lockSeats(SeatLockRequest request, UUID userId) {
        
        // 1. Validate User and Show
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        Show show = showRepository.findById(request.showId())
                .orElseThrow(() -> new IllegalArgumentException("Show not found"));

        // 2. Pre-Lock phase using Redis (Concurrency Guard)
        // If 10,000 users click the same seat, Redis ensures only 1 gets past this loop
        for (Long seatId : request.seatIds()) {
            String lockKey = String.format("lock:show:%d:seat:%d", request.showId(), seatId);
            boolean acquired = redisService.acquireLock(lockKey, Duration.ofMinutes(10));
            if (!acquired) {
                throw new ConflictException("Seat " + seatId + " is currently locked by another user. Please try again.");
            }
        }

        // 3. Database Check Phase
        // Fetch all seats for the show in one query (N+1 protected by our custom JPQL)
        List<ShowSeat> showSeats = showSeatRepository.findByShowIdWithSeat(request.showId());
        
        // Filter out the specific seats the user requested
        List<ShowSeat> targetSeats = showSeats.stream()
                .filter(ss -> request.seatIds().contains(ss.getSeat().getId()))
                .toList();

        if (targetSeats.size() != request.seatIds().size()) {
            throw new IllegalArgumentException("One or more requested seats do not exist for this show.");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (ShowSeat ss : targetSeats) {
            if (ss.getStatus() != ShowSeatStatus.AVAILABLE) {
                throw new ConflictException("Seat " + ss.getSeat().getId() + " is no longer available.");
            }
            totalAmount = totalAmount.add(ss.getPrice());
        }

        // 4. Create Pending Booking first so we have the ID to associate with ShowSeat
        Booking booking = Booking.builder()
                .user(user)
                .show(show)
                .totalAmount(totalAmount)
                .bookingTime(LocalDateTime.now())
                .status(BookingStatus.PENDING)
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        for (ShowSeat ss : targetSeats) {
            // Update status and set hold duration
            ss.setStatus(ShowSeatStatus.LOCKED);
            ss.setLockExpiration(LocalDateTime.now().plusMinutes(10));
            // Associate the seat with the newly created booking
            ss.setBooking(savedBooking);
        }

        // Save updated seats
        showSeatRepository.saveAll(targetSeats);

        return new BookingResponse(
                savedBooking.getId(),
                show.getId(),
                savedBooking.getStatus().name(),
                savedBooking.getTotalAmount()
        );
    }

    /**
     * Confirms a pending booking by marking its associated locked seats as BOOKED.
     * This finalizes the reservation process.
     *
     * @param bookingId the UUID of the pending booking to be confirmed
     * @throws IllegalArgumentException if the booking does not exist
     * @throws ConflictException if the booking is not in a PENDING state
     */
    @Transactional
    public void confirmBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new ConflictException("Booking is already processed. Current status: " + booking.getStatus());
        }

        // Finalize seats associated with this booking
        List<ShowSeat> bookedSeats = booking.getShowSeats();
        for (ShowSeat seat : bookedSeats) {
            seat.setStatus(ShowSeatStatus.BOOKED);
            seat.setLockExpiration(null);
        }
        showSeatRepository.saveAll(bookedSeats);

        booking.setStatus(BookingStatus.CONFIRMED);
        bookingRepository.save(booking);
    }

    @Transactional
    public void cancelBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ConflictException("Booking is already cancelled.");
        }

        // Release seats associated with this booking
        List<ShowSeat> bookedSeats = booking.getShowSeats();
        for (ShowSeat seat : bookedSeats) {
            seat.setStatus(ShowSeatStatus.AVAILABLE);
            seat.setLockExpiration(null);
            seat.setBooking(null);
        }
        showSeatRepository.saveAll(bookedSeats);

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }
}
