package ru.finess.finess.payment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record PaymentBeaconBluetoothId(
    @Column(name = "bluetooth_id", updatable = false, nullable = false) String value) {}
