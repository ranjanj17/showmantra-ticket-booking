package com.showmantra.dtos;

import java.util.List;

public record SeatMatrixResponse(
        Long showId,
        List<SeatInfo> seats
) {
    public record SeatInfo(
            Long seatId,
            String rowNumber,
            Integer seatNumber,
            String seatClass,
            String status
    ) {}
}
