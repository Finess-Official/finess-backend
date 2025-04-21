package ru.finess.finess.payment.domain;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.UUID;

@Embeddable
public record AccountId(
    @Column(name = "id", nullable = false, updatable = false, unique = true) UUID id) {

  public static AccountId random() {
    return new AccountId(UuidCreator.getTimeOrderedEpoch());
  }
}
