package ru.finess.finess.identity.domain;

import java.util.regex.Pattern;
import lombok.NonNull;

public record UserPassword(@NonNull String value) {

  public static final Pattern PASSWORD_PATTERN =
      Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{6,}$");

  public UserPassword {
    if (value.isBlank()) {
      throw new IllegalArgumentException("Password cannot be null or empty");
    }

    if (!PASSWORD_PATTERN.matcher(value).matches()) {
      throw new IllegalArgumentException(
          "Password must contain at least one lowercase letter, one uppercase letter, one digit, and be at least 6 characters long");
    }
  }
}
