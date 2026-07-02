package com.showmantra.dtos;

public record TheaterRequest(
        String name,
        Long cityId,
        String address
) {}
