package com.showmantra.repositories;

import com.showmantra.entities.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for managing Booking entities.
 */
public interface BookingRepository extends JpaRepository<Booking, UUID> {

    @Query("SELECT b FROM Booking b JOIN FETCH b.user JOIN FETCH b.show WHERE b.id = :bookingId")
    Optional<Booking> findByIdWithUserAndShow(@Param("bookingId") UUID bookingId);
}
