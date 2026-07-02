package com.showmantra.dtos;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRequest(
        UUID bookingId,
        BigDecimal amount,
        String paymentMethodDetails // e.g., simulated token or card info
) {}
