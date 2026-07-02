package com.showmantra.repositories;

import com.showmantra.entities.Theater;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

/**
 * Repository for managing Theater locations.
 */
public interface TheaterRepository extends JpaRepository<Theater, Long> {
    List<Theater> findByCityId(Long cityId);
}
