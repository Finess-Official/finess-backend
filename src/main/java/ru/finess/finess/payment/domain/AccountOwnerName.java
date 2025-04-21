package ru.finess.finess.payment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import lombok.NonNull;

@Embeddable
public record AccountOwnerName(
    @Column(name = "owner_name", nullable = false, updatable = false) @NonNull String value) {

  public AccountOwnerName {
    if (isValidName(value)) {
      throw new IllegalArgumentException("Invalid account owner name format: " + value);
    }
  }

  private boolean isValidName(String name) {
    return name.split(" ").length < 2 || !Objects.equals(name.trim(), name);
  }
}
