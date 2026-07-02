package com.showmantra.dtos;

import java.util.List;

/**
 * Response payload representing the complete layout of seats and their current booking statuses for a specific show.
 */
public record SeatMatrixResponse(
        Long showId,
        List<SeatInfo> seats
) {}
