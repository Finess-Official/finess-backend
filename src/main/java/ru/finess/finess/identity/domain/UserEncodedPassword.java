package ru.finess.finess.identity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;

@Embeddable
public record UserEncodedPassword(
    @Column(name = "encoded_password", nullable = false, updatable = false) String value) {

  public UserEncodedPassword {
    if (Objects.isNull(value) || value.isBlank()) {
      throw new IllegalArgumentException("Encoded password cannot be null or empty");
    }
  }
}
