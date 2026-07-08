package com.showmantra.agent;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents the current state of an active agent session.
 * This holds contextual information extracted from the conversation,
 * such as the selected movie, show, seats, and the status of the booking.
 */
@Data
public class AgentState {
    private String sessionId;
    private UUID userId;
    private String status = "INIT"; // INIT, MOVIE_SELECTED, SHOW_SELECTED, SEATS_LOCKED, PAYMENT_PENDING, CONFIRMED
    
    // Step-by-step context
    private String city;
    private LocalDate date;
    private Long movieId;
    private String movieName;
    
    // Selection context
    private Long showId;
    private String theaterName;
    private String showTime;
    
    // Transaction context
    private List<String> selectedSeats = new ArrayList<>();
    private UUID bookingId;
    private BigDecimal totalPrice;
}
