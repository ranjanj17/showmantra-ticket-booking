package com.showmantra.repositories;

import com.showmantra.entities.Theater;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for managing Theater locations.
 */
public interface TheaterRepository extends JpaRepository<Theater, Long> {
}
