package com.showmantra.dtos;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ShowRequest(
        Long movieId,
        Long screenId,
        LocalDateTime startTime,
        LocalDateTime endTime,
        BigDecimal basePrice
) {}
