package com.showmantra.repositories;

import com.showmantra.entities.Movie;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for managing Movie entities.
 */
public interface MovieRepository extends JpaRepository<Movie, Long> {
}
