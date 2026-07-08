package com.showmantra.repositories;

import com.showmantra.entities.ShowSeat;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Repository for managing ShowSeat entities, tracking real-time seat availability for shows.
 */
public interface ShowSeatRepository extends JpaRepository<ShowSeat, Long> {

    /**
     * Optimized query to fetch all seats for a specific show along with the physical seat layout data.
     * Prevents N+1 queries when building the SeatMatrixResponse.
     */
    @Query("SELECT ss FROM ShowSeat ss JOIN FETCH ss.seat WHERE ss.show.id = :showId")
    List<ShowSeat> findByShowIdWithSeat(@Param("showId") Long showId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ss FROM ShowSeat ss JOIN FETCH ss.seat WHERE ss.show.id = :showId AND ss.seat.id IN :seatIds")
    List<ShowSeat> findSeatsForUpdate(@Param("showId") Long showId, @Param("seatIds") List<Long> seatIds);
}
