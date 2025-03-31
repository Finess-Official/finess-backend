package ru.finess.finess.identity.domain;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public record UserId(
    @Column(name = "id", nullable = false, updatable = false, unique = true) UUID value) {

  public UserId {
    if (Objects.isNull(value)) {
      throw new IllegalArgumentException("User ID cannot be null");
    }
  }

  public static UserId random() {
    return new UserId(UuidCreator.getTimeOrderedEpoch());
  }
}
