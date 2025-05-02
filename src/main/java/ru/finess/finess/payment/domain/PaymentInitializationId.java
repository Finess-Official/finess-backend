package ru.finess.finess.payment.domain;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.UUID;

@Embeddable
public record PaymentInitializationId(
    @Column(name = "id", nullable = false, updatable = false, unique = true) UUID value) {
  public static PaymentInitializationId random() {
    return new PaymentInitializationId(UuidCreator.getTimeOrderedEpoch());
  }
}
