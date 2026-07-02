package com.showmantra.dtos;

import java.math.BigDecimal;

/**
 * DTO representing the basic information and status of a single seat.
 */
public record SeatInfo(
        Long seatId,
        String rowNumber,
        Integer seatNumber,
        String seatClass,
        BigDecimal price,
        String status
) {}
