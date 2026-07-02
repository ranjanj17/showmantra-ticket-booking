package com.showmantra.dtos;

/**
 * DTO representing the basic information and status of a single seat.
 */
public record SeatInfo(
        Long seatId,
        String rowNumber,
        Integer seatNumber,
        String seatClass,
        String status
) {}
