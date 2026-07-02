package com.showmantra.repositories;

import com.showmantra.entities.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ShowRepository extends JpaRepository<Show, Long> {

    @Query("SELECT s FROM Show s JOIN FETCH s.movie JOIN FETCH s.screen sc JOIN FETCH sc.theater WHERE s.id = :showId")
    Optional<Show> findByIdWithDetails(@Param("showId") Long showId);
}
