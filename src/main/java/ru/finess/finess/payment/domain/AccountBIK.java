package ru.finess.finess.payment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.regex.Pattern;
import lombok.NonNull;

@Embeddable
public record AccountBIK(
    @Column(name = "bik", nullable = false, updatable = false) @NonNull String value) {

  public static final Pattern BIK_PATTERN = Pattern.compile("^\\d{9}$");

  public AccountBIK {
    if (!BIK_PATTERN.matcher(value).matches()) {
      throw new IllegalArgumentException("Invalid BIK format: " + value);
    }
  }
}
