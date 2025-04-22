package ru.finess.finess.payment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import java.util.Objects;

@Embeddable
public record PaymentAmount(
    @Column(name = "amount", nullable = false, updatable = false) BigDecimal value) {

  public PaymentAmount {
    if (!isValid(value)) {
      throw new IllegalArgumentException("Invalid payment amount: " + value);
    }
  }

  private boolean isValid(BigDecimal value) {
    if (Objects.isNull(value)) {
      return false;
    }

    if (value.compareTo(BigDecimal.ZERO) <= 0) {
      return false;
    }

    return value.scale() <= 2;
  }
}
