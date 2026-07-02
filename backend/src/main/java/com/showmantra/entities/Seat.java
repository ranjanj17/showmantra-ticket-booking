package com.showmantra.entities;

import com.showmantra.entities.enums.SeatClass;

import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a physical chair inside a specific Screen.
 * This is static reference data defining the layout, independent of specific movie shows.
 */
@Entity
@Table(name = "seats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Seat extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screen_id", nullable = false)
    private Screen screen;

    @Column(nullable = false)
    private String rowNumber;

    @Column(nullable = false)
    private Integer seatNumber;

    // Ensures 'VIP' is stored as text. If we used ORDINAL (default), adding a new Enum at the top would break all existing DB rows.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SeatClass seatClass;
}
