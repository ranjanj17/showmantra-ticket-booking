package com.showmantra.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

import com.showmantra.entities.enums.BookingStatus;

/**
 * Represents a ticket reservation (pending or confirmed) made by a User for a specific Show.
 */
@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking extends BaseEntity {

    // Using UUID for public ticket IDs prevents competitors from knowing how many tickets we sell.
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // LAZY fetch prevents the N+1 query problem by not loading the User from DB until user.getEmail() is explicitly called.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // The specific movie screening being booked.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    // The total calculated price of all seats in this reservation.
    @Column(nullable = false)
    private BigDecimal totalAmount;

    // The exact timestamp when the reservation was initiated.
    @Column(nullable = false)
    private LocalDateTime bookingTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    // A single booking can hold multiple seats.
    // cascade = CascadeType.ALL means any database action (save, delete, update) 
    // applied to this Booking will automatically be applied to all its associated ShowSeats.
    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL)
    @Builder.Default
    private List<ShowSeat> showSeats = new ArrayList<>();
}
