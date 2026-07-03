package com.showmantra.repositories;

import com.showmantra.entities.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

/**
 * Repository for managing Movie entities.
 */
public interface MovieRepository extends JpaRepository<Movie, Long> {
    List<Movie> findByTitleContainingIgnoreCase(String title);

    @Query("SELECT DISTINCT m FROM Movie m JOIN Show s ON s.movie = m JOIN s.screen sc JOIN sc.theater t WHERE t.cityId = :cityId")
    List<Movie> findMoviesByCityId(@Param("cityId") Long cityId);
}
