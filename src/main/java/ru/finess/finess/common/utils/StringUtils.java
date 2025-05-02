package ru.finess.finess.common.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.SneakyThrows;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StringUtils {

  @SneakyThrows
  public static String sha256Digest(@NonNull String input) {
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    byte[] encodedHash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
    return bytesToHexString(encodedHash);
  }

  private static String bytesToHexString(byte[] encodedHash) {
    StringBuilder hexString = new StringBuilder();
    for (byte b : encodedHash) {
      String hex = Integer.toHexString(0xff & b);
      if (hex.length() == 1) {
        hexString.append('0');
      }
      hexString.append(hex);
    }
    return hexString.toString();
  }

  public static String toStringOrNull(Object obj) {
    return Optional.ofNullable(obj).map(Object::toString).orElse(null);
  }
}
