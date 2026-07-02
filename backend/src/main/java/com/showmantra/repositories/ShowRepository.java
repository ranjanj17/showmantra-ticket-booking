package com.showmantra.repositories;

import com.showmantra.entities.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing Show entities.
 */
public interface ShowRepository extends JpaRepository<Show, Long> {

    /**
     * Optimized query to fetch a Show along with its associated Movie, Screen, and Theater
     * in a single database round-trip to prevent N+1 query problems.
     */
    @Query("SELECT s FROM Show s JOIN FETCH s.movie JOIN FETCH s.screen sc JOIN FETCH sc.theater WHERE s.id = :showId")
    Optional<Show> findByIdWithDetails(@Param("showId") Long showId);

    /**
     * Optimized query to find all shows for a specific movie within a given date range.
     * Uses JOIN FETCH to eagerly load related entities and prevent N+1 queries.
     */
    @Query("SELECT s FROM Show s JOIN FETCH s.movie JOIN FETCH s.screen sc JOIN FETCH sc.theater WHERE s.movie.id = :movieId AND s.startTime >= :startOfDay AND s.startTime < :endOfDay")
    List<Show> findShowsByMovieIdAndDate(@Param("movieId") Long movieId, @Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);
}
