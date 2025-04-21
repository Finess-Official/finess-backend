package ru.finess.finess.payment.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.regex.Pattern;
import lombok.NonNull;

@Embeddable
public record AccountINN(
    @Column(name = "inn", nullable = false, updatable = false) @NonNull String value) {

  public static final Pattern INN_PATTERN = Pattern.compile("^(\\d{12}|\\d{10})$");

  public AccountINN {
    if (!INN_PATTERN.matcher(value).matches()) {
      throw new IllegalArgumentException("Invalid INN format: " + value);
    }
  }
}
