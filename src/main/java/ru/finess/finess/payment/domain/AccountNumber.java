package ru.finess.finess.payment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.regex.Pattern;
import lombok.NonNull;

@Embeddable
public record AccountNumber(
    @Column(name = "number", nullable = false, updatable = false) @NonNull String value) {

  public static final Pattern ACCOUNT_NUMBER_PATTERN = Pattern.compile("^(\\d{20}|\\d{22})$");

  public AccountNumber {
    if (!ACCOUNT_NUMBER_PATTERN.matcher(value).matches()) {
      throw new IllegalArgumentException("Invalid account number format: " + value);
    }
  }
}
