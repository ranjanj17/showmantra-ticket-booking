package com.showmantra.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * A detailed summary of a user's booking, used for displaying booking history.
 */
public record BookingHistoryResponse(
        UUID bookingId,
        String movieTitle,
        String theaterName,
        String screenName,
        LocalDateTime showTime,
        List<String> bookedSeats,
        BigDecimal totalAmount,
        String status,
        LocalDateTime bookedAt
) {}
