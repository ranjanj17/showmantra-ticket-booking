package com.showmantra.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.showmantra.entities.enums.ShowSeatStatus;

/**
 * The transactional fulcrum of the ticketing system.
 * Represents the state (AVAILABLE, LOCKED, BOOKED) of a specific physical Seat for a specific Show.
 */
@Entity
@Table(
    name = "show_seats",
    uniqueConstraints = {
        // CRITICAL: Prevents double-booking at the database level by ensuring a physical seat only exists once per show.
        @UniqueConstraint(name = "uk_show_seat", columnNames = {"show_id", "seat_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShowSeat extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FetchType.LAZY ensures we only load the Show data when absolutely necessary, saving massive RAM during concurrent loads.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "show_id", nullable = false)
    private Show show;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    // Saving as STRING makes direct SQL queries readable (e.g. SELECT * WHERE status='LOCKED')
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShowSeatStatus status;

    // If status is LOCKED, this defines when the temporary hold (e.g. 10 mins) expires and the seat becomes AVAILABLE again.
    private LocalDateTime lockExpiration;

    // The dynamically calculated price for this specific seat (e.g. VIP seats cost more than STANDARD).
    @Column(nullable = false)
    private BigDecimal price;
}
