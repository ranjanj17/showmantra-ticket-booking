package com.showmantra.services;

import com.showmantra.agent.exceptions.MissingSlotException;
import com.showmantra.agent.exceptions.SeatConflictException;
import com.showmantra.dtos.BookingResponse;
import com.showmantra.dtos.SeatLockRequest;
import com.showmantra.dtos.ShowtimeResponse;
import com.showmantra.entities.Movie;
import com.showmantra.entities.ShowSeat;
import com.showmantra.entities.enums.ShowSeatStatus;
import com.showmantra.repositories.MovieRepository;
import com.showmantra.repositories.ShowSeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class DeterministicBookingService {

    private final MovieRepository movieRepository;
    private final ShowService showService;
    private final ShowSeatRepository showSeatRepository;
    private final BookingService bookingService;

    public Long searchMovie(String title) {
        if (title == null || title.isBlank()) {
            throw new MissingSlotException("Please specify the movie name.");
        }
        List<Movie> movies = movieRepository.findByTitleContainingIgnoreCase(title);
        if (movies.isEmpty()) {
            throw new IllegalArgumentException("Could not find movie: " + title);
        }
        return movies.get(0).getId();
    }

    public ShowtimeResponse findBestShow(Long movieId, LocalDate date, String timePref) {
        if (movieId == null) throw new MissingSlotException("Movie ID is missing.");
        if (date == null) throw new MissingSlotException("Date is missing.");
        if (timePref == null) throw new MissingSlotException("Time preference is missing (e.g. morning, afternoon, evening).");

        List<ShowtimeResponse> shows = showService.getShowsByMovieAndDate(movieId, date);
        if (shows.isEmpty()) {
            throw new IllegalArgumentException("No shows available for the selected date.");
        }

        // Simple heuristic: just return the first one that vaguely matches, 
        // or just the first available if filtering is complex.
        // For production, parse timePref to find the closest match.
        for (ShowtimeResponse s : shows) {
            int hour = s.startTime().getHour();
            if (timePref.equalsIgnoreCase("morning") && hour < 12) return s;
            if (timePref.equalsIgnoreCase("afternoon") && hour >= 12 && hour < 17) return s;
            if (timePref.equalsIgnoreCase("evening") && hour >= 17) return s;
        }

        return shows.get(0); // Fallback to first show
    }

    public List<ShowSeat> selectBestSeats(Long showId, Integer seatCount) {
        if (showId == null) throw new MissingSlotException("Show ID is missing.");
        if (seatCount == null || seatCount < 1) throw new MissingSlotException("Please specify how many seats you need.");

        List<ShowSeat> allSeats = showSeatRepository.findByShowIdWithSeat(showId);
        List<ShowSeat> available = allSeats.stream()
                .filter(s -> s.getStatus() == ShowSeatStatus.AVAILABLE)
                .collect(Collectors.toList());

        if (available.size() < seatCount) {
            throw new IllegalArgumentException("Not enough seats available. Only " + available.size() + " left.");
        }

        // Return the first N available seats (a real algorithm would find contiguous ones)
        return available.stream()
                .limit(seatCount)
                .collect(Collectors.toList());
    }

    public BookingResponse lockSeats(Long showId, List<Long> seatIds, UUID userId) {
        try {
            return bookingService.lockSeats(new SeatLockRequest(showId, seatIds), userId);
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("no longer available")) {
                throw new SeatConflictException("Seats were taken. Need to retry.");
            }
            throw e;
        }
    }

    public String getSeatLayout(Long showId) {
        if (showId == null) throw new MissingSlotException("Show ID is missing.");
        List<ShowSeat> allSeats = showSeatRepository.findByShowIdWithSeat(showId);
        List<ShowSeat> available = allSeats.stream()
                .filter(s -> s.getStatus() == ShowSeatStatus.AVAILABLE)
                .collect(Collectors.toList());
        
        if (available.isEmpty()) {
            return "No seats available for this show.";
        }

        Map<BigDecimal, List<ShowSeat>> grouped = available.stream().collect(Collectors.groupingBy(ShowSeat::getPrice));
        
        StringBuilder sb = new StringBuilder();
        grouped.forEach((price, seats) -> {
            sb.append("Price $").append(price).append(": ");
            String seatLabels = seats.stream().map(s -> s.getSeat().getRowNumber() + s.getSeat().getSeatNumber()).collect(Collectors.joining(", "));
            sb.append(seatLabels).append("\n");
        });
        
        return sb.toString();
    }

    public BookingResponse lockSpecificSeats(Long showId, List<String> seatLabels, UUID userId) {
        if (showId == null) throw new MissingSlotException("Show ID is missing.");
        if (seatLabels == null || seatLabels.isEmpty()) throw new MissingSlotException("Seat labels are missing.");
        
        List<ShowSeat> allSeats = showSeatRepository.findByShowIdWithSeat(showId);
        List<Long> seatIds = allSeats.stream()
                .filter(s -> seatLabels.contains(s.getSeat().getRowNumber() + s.getSeat().getSeatNumber()))
                .map(s -> s.getSeat().getId())
                .collect(Collectors.toList());
                
        if (seatIds.size() != seatLabels.size()) {
            throw new IllegalArgumentException("Some seats are invalid or not found.");
        }
        
        return lockSeats(showId, seatIds, userId);
    }

    public void processPayment(UUID bookingId) {
        if (bookingId == null) throw new MissingSlotException("Booking ID is missing.");
        bookingService.confirmBooking(bookingId);
    }

    public List<Movie> listMovies() {
        return movieRepository.findAll();
    }

    public List<ShowtimeResponse> listShows(Long movieId, LocalDate date) {
        if (movieId == null) throw new MissingSlotException("Movie ID is missing.");
        if (date == null) throw new MissingSlotException("Date is missing.");
        return showService.getShowsByMovieAndDate(movieId, date);
    }

    public void cancelBooking(UUID bookingId) {
        if (bookingId == null) throw new MissingSlotException("Booking ID is missing.");
        bookingService.cancelBooking(bookingId);
    }
}
