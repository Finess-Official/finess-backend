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
import ru.finess.finess.identity.domain.Session;
import ru.finess.finess.identity.domain.SessionToken;
import ru.finess.finess.identity.domain.UserId;

@Slf4j
@Component
public class JwtValidator {

  private final JwtParser accessTokenParser;
  private final JwtParser refreshTokenParser;

  public JwtValidator(
      @Value("${session.jwt.access.key}") String accessKey,
      @Value("${session.jwt.refresh.key}") String refreshKey) {

    this.accessTokenParser =
        Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(accessKey.getBytes())).build();
    this.refreshTokenParser =
        Jwts.parserBuilder().setSigningKey(Keys.hmacShaKeyFor(refreshKey.getBytes())).build();
  }

  public Optional<Session> validateSession(@NonNull String rawAccessToken) {
    try {
      Jws<Claims> claimsJws = accessTokenParser.parseClaimsJws(rawAccessToken);
      Claims body = claimsJws.getBody();
      OffsetDateTime expirationDate = DateUtils.toOffsetDateTime(body.getExpiration());
      UserId userId = UserId.fromString(body.getSubject());

      SessionToken accessToken = new SessionToken(rawAccessToken, expirationDate);
      return Optional.of(Session.builder().user(userId).accessToken(accessToken).build());
    } catch (JwtException | IllegalArgumentException e) {
      log.error("Invalid JWT accessToken: {}", e.getMessage());
      return Optional.empty();
    }
  }

  public Optional<UserId> getSubject(@NonNull String rawRefreshToken) {
    try {
      Jws<Claims> claimsJws = refreshTokenParser.parseClaimsJws(rawRefreshToken);
      Claims body = claimsJws.getBody();
      UserId userId = UserId.fromString(body.getSubject());

      return Optional.of(userId);
    } catch (JwtException | IllegalArgumentException e) {
      log.error("Invalid JWT refreshToken: {}", e.getMessage());
      return Optional.empty();
    }
  }
}
