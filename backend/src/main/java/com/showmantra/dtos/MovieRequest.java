package com.showmantra.dtos;

import java.time.LocalDate;

public record MovieRequest(
        String title,
        Integer durationMinutes,
        String language,
        String genre,
        LocalDate releaseDate
) {}
