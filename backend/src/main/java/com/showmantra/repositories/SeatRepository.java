package com.showmantra.repositories;

import com.showmantra.entities.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByScreenId(Long screenId);
}
