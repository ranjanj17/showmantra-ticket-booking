package com.showmantra.agent.tools;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import java.util.function.Function;
import com.showmantra.services.DeterministicBookingService;
import com.showmantra.agent.StateRepository;
import com.showmantra.agent.AgentState;
import com.showmantra.dtos.BookingResponse;
import lombok.RequiredArgsConstructor;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import com.showmantra.entities.Movie;
import com.showmantra.dtos.ShowtimeResponse;
import com.showmantra.repositories.UserRepository;
import com.showmantra.entities.User;

/**
 * Configuration class that defines various functions (tools) available to the AI agent.
 * These functions allow the LLM to interact with backend services to perform actions
 * like listing movies, finding shows, booking seats, and cancelling bookings.
 */
@Configuration
@RequiredArgsConstructor
public class AgentTools {

    private final DeterministicBookingService backendLogic;
    private final StateRepository stateRepo;
    private final UserRepository userRepository;

    public record EmptyRequest(String sessionId) {}
    public record ListMoviesResponse(String status, List<String> movies) {}

    public record SearchShowsRequest(String sessionId, String movie, String city, String date) {}
    public record SearchShowsResponse(String status, List<String> shows, String message) {}

    public record GetSeatLayoutRequest(String sessionId, Long showId) {}
    public record GetSeatLayoutResponse(String status, String layout, String message) {}

    public record LockSeatsRequest(String sessionId, Long showId, List<String> seatLabels) {}
    public record LockSeatsResponse(String status, String message, UUID bookingId, Double amount) {}

    public record ProcessPaymentRequest(String sessionId, UUID bookingId) {}
    public record ProcessPaymentResponse(String status, String message) {}

    public record CancelBookingRequest(String sessionId, String bookingId) {}
    public record CancelBookingResponse(String status, String message) {}

    /**
     * Tool to retrieve a list of all currently playing movies.
     * 
     * @return a Function that responds with the list of available movies
     */
    @Bean
    @Description("List all movies currently available for booking. Useful when the user asks what movies are playing.")
    public Function<EmptyRequest, ListMoviesResponse> listMovies() {
        return request -> {
            try {
                List<String> movieTitles = backendLogic.listMovies().stream()
                        .map(Movie::getTitle)
                        .collect(Collectors.toList());
                return new ListMoviesResponse("SUCCESS", movieTitles);
            } catch (Exception e) {
                return new ListMoviesResponse("FAILED", List.of(e.getMessage()));
            }
        };
    }

    @Bean
    @Description("Search for shows for a given movie, city, and date. MUST provide a valid date in YYYY-MM-DD format.")
    public Function<SearchShowsRequest, SearchShowsResponse> searchShows() {
        return request -> {
            try {
                AgentState state = stateRepo.findById(request.sessionId()).orElse(new AgentState());
                state.setSessionId(request.sessionId());
                state.setMovieName(request.movie());
                state.setCity(request.city());
                state.setDate(LocalDate.parse(request.date()));
                
                Long movieId = backendLogic.searchMovie(request.movie());
                state.setMovieId(movieId);
                state.setStatus("MOVIE_SELECTED");
                stateRepo.save(state);

                List<ShowtimeResponse> shows = backendLogic.listShows(movieId, LocalDate.parse(request.date()));
                List<String> formattedShows = shows.stream()
                        .map(s -> "Show ID: " + s.showId() + " | Time: " + s.startTime().toLocalTime().toString() + " at " + s.theaterName() + ", Screen: " + s.screenName())
                        .collect(Collectors.toList());
                return new SearchShowsResponse("SUCCESS", formattedShows, "Found shows. Present the theaters and times to the user to choose (DO NOT show the Show ID).");
            } catch (Exception e) {
                return new SearchShowsResponse("FAILED", List.of(), e.getMessage());
            }
        };
    }

    @Bean
    @Description("Get the seat layout and available seats for a specific show ID. MUST be called after the user selects a show.")
    public Function<GetSeatLayoutRequest, GetSeatLayoutResponse> getSeatLayout() {
        return request -> {
            try {
                AgentState state = stateRepo.findById(request.sessionId()).orElseThrow(() -> new RuntimeException("Session state not found"));
                state.setShowId(request.showId());
                state.setStatus("SHOW_SELECTED");
                stateRepo.save(state);

                String layout = backendLogic.getSeatLayout(request.showId());
                return new GetSeatLayoutResponse("SUCCESS", layout, "Present the available seat categories and prices. Ask the user which category and how many tickets they want. DO NOT ask for specific seat numbers.");
            } catch (Exception e) {
                return new GetSeatLayoutResponse("FAILED", null, e.getMessage());
            }
        };
    }

    @Bean
    @Description("Lock specific seats after the user has chosen them. MUST provide showId and a list of seat labels like ['A1', 'A2'].")
    public Function<LockSeatsRequest, LockSeatsResponse> lockSeats() {
        return request -> {
            try {
                AgentState state = stateRepo.findById(request.sessionId()).orElseThrow(() -> new RuntimeException("Session state not found"));
                
                // Retrieve the authenticated user's ID from the SecurityContext
                String principalName = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
                UUID userId = UUID.fromString(principalName);
                
                state.setUserId(userId);
                BookingResponse booking = backendLogic.lockSpecificSeats(request.showId(), request.seatLabels(), userId);
                
                state.setSelectedSeats(request.seatLabels());
                state.setBookingId(booking.bookingId());
                state.setTotalPrice(booking.totalAmount());
                state.setStatus("SEATS_LOCKED");
                stateRepo.save(state);

                return new LockSeatsResponse("SUCCESS", "Successfully locked seats. Prompt the user to confirm payment of " + booking.totalAmount() + " for these seats.", booking.bookingId(),
                        booking.totalAmount().doubleValue());
            } catch (Exception e) {
                return new LockSeatsResponse("FAILED", e.getMessage(), null, null);
            }
        };
    }

    @Bean
    @Description("Process payment for a booking after the user confirms the price. MUST provide bookingId.")
    public Function<ProcessPaymentRequest, ProcessPaymentResponse> processPayment() {
        return request -> {
            try {
                AgentState state = stateRepo.findById(request.sessionId()).orElseThrow(() -> new RuntimeException("Session state not found"));
                backendLogic.processPayment(request.bookingId());
                
                state.setStatus("CONFIRMED");
                stateRepo.save(state);
                
                return new ProcessPaymentResponse("SUCCESS", "Payment processed and booking confirmed.");
            } catch (Exception e) {
                return new ProcessPaymentResponse("FAILED", e.getMessage());
            }
        };
    }

    /**
     * Tool to cancel an existing booking.
     * 
     * @return a Function that processes the cancellation of a booking by its ID
     */
    @Bean
    @Description("Cancel a booking given the booking ID. MUST provide the bookingId.")
    public Function<CancelBookingRequest, CancelBookingResponse> cancelBooking() {
        return request -> {
            try {
                backendLogic.cancelBooking(UUID.fromString(request.bookingId()));
                return new CancelBookingResponse("SUCCESS", "Booking " + request.bookingId() + " successfully cancelled.");
            } catch (Exception e) {
                return new CancelBookingResponse("FAILED", e.getMessage());
            }
        };
    }
}
