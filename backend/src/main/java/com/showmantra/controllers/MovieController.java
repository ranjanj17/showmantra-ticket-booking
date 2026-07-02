package com.showmantra.controllers;

import com.showmantra.dtos.MovieRequest;
import com.showmantra.entities.Movie;
import com.showmantra.services.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @PostMapping
    public ResponseEntity<Movie> createMovie(@RequestBody MovieRequest request) {
        Movie movie = movieService.createMovie(request);
        return new ResponseEntity<>(movie, HttpStatus.CREATED);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Movie>> searchMovies(@RequestParam String q) {
        return ResponseEntity.ok(movieService.searchMovies(q));
    }

    @GetMapping
    public ResponseEntity<List<Movie>> getAllMovies() {
        return ResponseEntity.ok(movieService.getAllMovies());
    }
}
