package com.showmantra.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Represents a movie catalog entry in the ticketing system.
 */
@Entity
@Table(name = "movies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Integer durationMinutes;

    private String language;
    private String genre;
    private LocalDate releaseDate;
}
