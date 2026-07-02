package com.showmantra.entities;

import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a specific viewing hall (screen) inside a Theater.
 * Belongs to one Theater, and contains many physical Seats.
 */
@Entity
@Table(name = "screens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Screen extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theater_id", nullable = false)
    private Theater theater;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer totalCapacity;
}
