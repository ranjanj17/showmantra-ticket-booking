package com.showmantra.controllers;

import com.showmantra.dtos.SeatMatrixResponse;
import com.showmantra.dtos.ShowRequest;
import com.showmantra.dtos.ShowtimeResponse;
import com.showmantra.entities.Show;
import com.showmantra.services.ShowService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/shows")
@RequiredArgsConstructor
public class ShowController {

    private final ShowService showService;

    @PostMapping
    public ResponseEntity<Show> createShow(@RequestBody ShowRequest request) {
        Show show = showService.createShow(request);
        return new ResponseEntity<>(show, HttpStatus.CREATED);
    }

    @GetMapping("/movie/{movieId}")
    public ResponseEntity<List<ShowtimeResponse>> getShowsByMovieAndDate(
            @PathVariable Long movieId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<ShowtimeResponse> shows = showService.getShowsByMovieAndDate(movieId, date);
        return ResponseEntity.ok(shows);
    }

    @GetMapping("/{showId}/seats")
    public ResponseEntity<SeatMatrixResponse> getSeatMatrix(@PathVariable Long showId) {
        SeatMatrixResponse seatMatrix = showService.getSeatMatrix(showId);
        return ResponseEntity.ok(seatMatrix);
    }
}
