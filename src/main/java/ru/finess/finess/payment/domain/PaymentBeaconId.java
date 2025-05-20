package ru.finess.finess.payment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.UUID;

@Embeddable
public record PaymentBeaconId(
    @Column(name = "id", nullable = false, updatable = false, unique = true) UUID value) {

  public static PaymentBeaconId random() {
    return new PaymentBeaconId(UUID.randomUUID());
  }
}
