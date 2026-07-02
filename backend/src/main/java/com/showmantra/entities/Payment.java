package com.showmantra.entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

import com.showmantra.entities.enums.PaymentStatus;

/**
 * Represents the financial transaction/payment attempt linked to a specific Booking.
 */
@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {

    // Using UUID ensures payment transactions are unguessable and globally unique.
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false, unique = true)
    private Booking booking;

    // The external transaction ID returned by the payment gateway (e.g., Stripe, Razorpay).
    private String providerReference;

    // The actual amount charged in this transaction attempt.
    @Column(nullable = false)
    private BigDecimal amount;

    // EnumType.STRING saves 'SUCCESS' in the DB instead of '0', preventing data corruption if enum order changes.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;
}
