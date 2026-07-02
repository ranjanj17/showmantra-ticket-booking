package com.showmantra.controllers;

import com.showmantra.dtos.TheaterRequest;
import com.showmantra.entities.Theater;
import com.showmantra.services.TheaterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/theaters")
@RequiredArgsConstructor
public class TheaterController {

    private final TheaterService theaterService;

    @PostMapping
    public ResponseEntity<Theater> createTheater(@RequestBody TheaterRequest request) {
        Theater theater = theaterService.createTheater(request);
        return new ResponseEntity<>(theater, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Theater>> getTheaters(@RequestParam(required = false) Long cityId) {
        if (cityId != null) {
            return ResponseEntity.ok(theaterService.getTheatersByCityId(cityId));
        }
        return ResponseEntity.ok(theaterService.getAllTheaters());
    }
}
