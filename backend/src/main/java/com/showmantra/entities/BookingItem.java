package com.showmantra.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Represents a historical record of a specific seat booked in a specific booking.
 * This separates the booking record from the physical ShowSeat, allowing seats to be released
 * upon cancellation while preserving the booking history.
 */
@Entity
@Table(name = "booking_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_seat_id", nullable = false)
    private ShowSeat showSeat;

    // Preserves the price that was actually paid at the time of booking,
    // protecting against future price changes for the ShowSeat.
    @Column(nullable = false)
    private BigDecimal priceAtBooking;
}
