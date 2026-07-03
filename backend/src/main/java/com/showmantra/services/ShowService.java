package com.showmantra.services;

import com.showmantra.dtos.SeatInfo;
import com.showmantra.dtos.SeatMatrixResponse;
import com.showmantra.dtos.ShowRequest;
import com.showmantra.dtos.ShowtimeResponse;
import com.showmantra.entities.Movie;
import com.showmantra.entities.Screen;
import com.showmantra.entities.Seat;
import com.showmantra.entities.Show;
import com.showmantra.entities.ShowSeat;
import com.showmantra.entities.enums.SeatClass;
import com.showmantra.entities.enums.ShowSeatStatus;
import com.showmantra.repositories.MovieRepository;
import com.showmantra.repositories.ScreenRepository;
import com.showmantra.repositories.SeatRepository;
import com.showmantra.repositories.ShowRepository;
import com.showmantra.repositories.ShowSeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShowService {

    private final ShowRepository showRepository;
    private final MovieRepository movieRepository;
    private final ScreenRepository screenRepository;
    private final SeatRepository seatRepository;
    private final ShowSeatRepository showSeatRepository;

    @Transactional
    public Show createShow(ShowRequest request) {
        Movie movie = movieRepository.findById(request.movieId())
                .orElseThrow(() -> new IllegalArgumentException("Movie not found"));

        Screen screen = screenRepository.findById(request.screenId())
                .orElseThrow(() -> new IllegalArgumentException("Screen not found"));

        Show show = Show.builder()
                .movie(movie)
                .screen(screen)
                .startTime(request.startTime())
                .endTime(request.endTime())
                .build();

        Show savedShow = showRepository.save(show);

        // Fetch all physical seats for this screen
        List<Seat> seats = seatRepository.findByScreenId(screen.getId());

        // Initialize ShowSeats for the new show
        List<ShowSeat> showSeats = seats.stream().map(seat -> {
            // Price logic depends on SeatClass
            BigDecimal finalPrice = request.basePrice();
            if (seat.getSeatClass() == SeatClass.GOLD) {
                finalPrice = finalPrice.add(new BigDecimal("50.00"));
            } else if (seat.getSeatClass() == SeatClass.PLATINUM) {
                finalPrice = finalPrice.add(new BigDecimal("100.00"));
            }
            
            return ShowSeat.builder()
                    .show(savedShow)
                    .seat(seat)
                    .status(ShowSeatStatus.AVAILABLE)
                    .price(finalPrice) 
                    .build();
        }).collect(Collectors.toList());

        showSeatRepository.saveAll(showSeats);

        return savedShow;
    }

    public List<ShowtimeResponse> getShowsByMovieAndDate(Long movieId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        
        List<Show> shows = showRepository.findShowsByMovieIdAndDate(movieId, startOfDay, endOfDay);

        return shows.stream().map(show -> new ShowtimeResponse(
                show.getId(),
                show.getStartTime(),
                show.getMovie().getTitle(),
                show.getScreen().getTheater().getName(),
                show.getScreen().getName()
        )).collect(Collectors.toList());
    }

    public SeatMatrixResponse getSeatMatrix(Long showId) {
        List<ShowSeat> showSeats = showSeatRepository.findByShowIdWithSeat(showId);
        
        List<SeatInfo> seatInfos = showSeats.stream().map(ss -> new SeatInfo(
                ss.getSeat().getId(),
                ss.getSeat().getRowNumber(),
                ss.getSeat().getSeatNumber(),
                ss.getSeat().getSeatClass().name(),
                ss.getPrice(),
                ss.getStatus().name()
        )).collect(Collectors.toList());

        return new SeatMatrixResponse(showId, seatInfos);
    }
}
