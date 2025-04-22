package ru.finess.finess.identity.domain;

import java.time.OffsetDateTime;
import lombok.NonNull;

public record SessionToken(@NonNull String value, @NonNull OffsetDateTime expirationTime) {
  public SessionToken {
    if (value.isBlank()) {
      throw new IllegalArgumentException("Session token cannot be blank");
    }
  }
}
