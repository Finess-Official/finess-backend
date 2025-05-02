package ru.finess.finess.common.utils;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class HttpUtils {

  private static final Pattern BEARER_PATTERN = Pattern.compile("Bearer (.+)");

  public static Optional<String> getAccessTokenFromRequest(HttpServletRequest servletRequest) {
    String bearer = servletRequest.getHeader("Authorization");
    if (bearer == null) {
      return Optional.empty();
    }
    Matcher matcher = BEARER_PATTERN.matcher(bearer);
    return matcher.find() ? Optional.ofNullable(matcher.group(1)) : Optional.empty();
  }
}
