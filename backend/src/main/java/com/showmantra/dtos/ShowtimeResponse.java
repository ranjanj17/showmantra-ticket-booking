package com.showmantra.dtos;

import java.time.LocalDateTime;

/**
 * Flattened response payload containing high-level details of a scheduled show.
 */
public record ShowtimeResponse(
        Long showId,
        LocalDateTime startTime,
        String movieTitle,
        String theaterName,
        String screenName
) {}
