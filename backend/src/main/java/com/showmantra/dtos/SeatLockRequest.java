package com.showmantra.dtos;

import java.util.List;

/**
 * Request payload for attempting to lock a list of seats for a specific show.
 */
public record SeatLockRequest(
        Long showId,
        List<Long> seatIds
) {}
