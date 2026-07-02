package com.showmantra.dtos;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response payload returned after successfully locking seats.
 */
public record BookingResponse(
        UUID bookingId,
        Long showId,
        String status,
        BigDecimal totalAmount
) {}
