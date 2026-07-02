package com.showmantra.repositories;

import com.showmantra.entities.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing Booking entities.
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    // Fetches a single Booking with User and Show eagerly loaded to prevent N+1 queries.
    @Query("SELECT b FROM Booking b JOIN FETCH b.user JOIN FETCH b.show WHERE b.id = :bookingId")
    Optional<Booking> findByIdWithUserAndShow(@Param("bookingId") UUID bookingId);

    // Eagerly loads the entire booking history (Movie, Theater, Screen, and Seats) for a user 
    // in a single query to avoid multiple separate database calls.
    @Query("SELECT DISTINCT b FROM Booking b " +
           "JOIN FETCH b.show s " +
           "JOIN FETCH s.movie m " +
           "JOIN FETCH s.screen sc " +
           "JOIN FETCH sc.theater t " +
           "LEFT JOIN FETCH b.items items " +
           "LEFT JOIN FETCH items.showSeat ss " +
           "LEFT JOIN FETCH ss.seat st " +
           "WHERE b.user.id = :userId " +
           "ORDER BY b.bookingTime DESC")
    List<Booking> findAllByUserIdWithDetails(@Param("userId") UUID userId);
}
