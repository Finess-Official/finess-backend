package ru.finess.finess.identity.infrastructure;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.time.OffsetDateTime;
import java.util.Optional;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.finess.finess.common.date.DateUtils;
import ru.finess.finess.identity.application.Session;
import ru.finess.finess.identity.application.SessionToken;
import ru.finess.finess.identity.domain.UserId;

@Slf4j
@Component
public class JwtValidator {

  private final JwtParser parser;

  public JwtValidator(@Value("${session.jwt.access.key}") String secretKey) {
    this.parser =
        Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes())).build();
  }

  public Optional<Session> validateSession(@NonNull String accessToken) {
    try {
      Jws<Claims> claimsJws = parser.parseClaimsJws(accessToken);
      Claims body = claimsJws.getBody();
      OffsetDateTime expirationDate = DateUtils.toOffsetDateTime(body.getExpiration());
      UserId userId = UserId.fromString(body.getSubject());

      SessionToken token = new SessionToken(accessToken, expirationDate);
      return Optional.of(new Session(userId, token));
    } catch (JwtException | IllegalArgumentException e) {
      log.error("Invalid JWT token: {}", e.getMessage());
      return Optional.empty();
    }
  }
}
