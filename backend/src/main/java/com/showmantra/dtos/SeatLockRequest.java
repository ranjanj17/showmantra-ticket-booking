package com.showmantra.dtos;

import java.util.List;

public record SeatLockRequest(
        Long showId,
        List<Long> seatIds
) {}
