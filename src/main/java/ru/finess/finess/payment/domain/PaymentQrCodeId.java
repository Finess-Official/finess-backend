package ru.finess.finess.payment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record PaymentQrCodeId(
    @Column(name = "id", nullable = false, updatable = false, unique = true) String value) {}
