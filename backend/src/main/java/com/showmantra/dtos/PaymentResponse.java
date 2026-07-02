package com.showmantra.dtos;

import java.util.UUID;

public record PaymentResponse(
        UUID paymentId,
        UUID bookingId,
        String status,
        String message
) {}
