package ru.finess.finess.payment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record AcquiringPaymentId(
    @Column(name = "acquiring_payment_id", nullable = false, updatable = false, unique = true)
        String value) {}
