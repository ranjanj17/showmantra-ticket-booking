package com.showmantra.dtos;

import java.time.LocalDateTime;

public record ShowtimeResponse(
        Long showId,
        LocalDateTime startTime,
        String movieTitle,
        String theaterName,
        String screenName
) {}
