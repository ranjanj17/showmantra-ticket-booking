package com.showmantra.services;

import com.showmantra.dtos.MovieRequest;
import com.showmantra.entities.Movie;
import com.showmantra.repositories.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;

    public Movie createMovie(MovieRequest request) {
        Movie movie = Movie.builder()
                .title(request.title())
                .durationMinutes(request.durationMinutes())
                .language(request.language())
                .genre(request.genre())
                .releaseDate(request.releaseDate())
                .build();
        return movieRepository.save(movie);
    }

    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    public List<Movie> searchMovies(String title) {
        return movieRepository.findByTitleContainingIgnoreCase(title);
    }
}
