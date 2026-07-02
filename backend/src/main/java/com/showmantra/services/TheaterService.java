package com.showmantra.services;

import com.showmantra.dtos.TheaterRequest;
import com.showmantra.entities.Theater;
import com.showmantra.repositories.TheaterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TheaterService {

    private final TheaterRepository theaterRepository;

    public Theater createTheater(TheaterRequest request) {
        Theater theater = Theater.builder()
                .name(request.name())
                .cityId(request.cityId())
                .address(request.address())
                .build();
        return theaterRepository.save(theater);
    }

    public List<Theater> getAllTheaters() {
        return theaterRepository.findAll();
    }

    public List<Theater> getTheatersByCityId(Long cityId) {
        return theaterRepository.findByCityId(cityId);
    }
}
