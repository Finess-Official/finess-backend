package ru.finess.finess.identity.infrastructure;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.finess.finess.identity.application.TokenConstructor;
import ru.finess.finess.identity.domain.SessionToken;
import ru.finess.finess.identity.domain.UserId;

@Component
public class JwtTokenConstructor implements TokenConstructor {

  private final Duration accessExpirationDuration;
  private final SecretKey accessKey;

  private final Duration refreshExpirationDuration;
  private final SecretKey refreshKey;

  public JwtTokenConstructor(
      @Value("${session.jwt.access.key}") String accessSecretKey,
      @Value("${session.jwt.access.expirationDuration}") Duration accessExpirationDuration,
      @Value("${session.jwt.refresh.key}") String refreshSecretKey,
      @Value("${session.jwt.refresh.expirationDuration}") Duration refreshExpirationDuration) {
    this.accessKey = Keys.hmacShaKeyFor(accessSecretKey.getBytes());
    this.accessExpirationDuration = accessExpirationDuration;
    this.refreshKey = Keys.hmacShaKeyFor(refreshSecretKey.getBytes());
    this.refreshExpirationDuration = refreshExpirationDuration;
  }

  @Override
  public SessionToken createAccessToken(
      @NonNull UserId userId, @NonNull OffsetDateTime currentTime) {
    OffsetDateTime expirationTime = currentTime.plus(this.accessExpirationDuration);
    String token = createToken(userId, currentTime, expirationTime, accessKey);
    return new SessionToken(token, expirationTime);
  }

  @Override
  public SessionToken createRefreshToken(
      @NonNull UserId userId, @NonNull OffsetDateTime currentTime) {
    OffsetDateTime expirationTime = currentTime.plus(this.refreshExpirationDuration);
    String token = createToken(userId, currentTime, expirationTime, refreshKey);
    return new SessionToken(token, expirationTime);
  }

  private String createToken(
      UserId userId,
      OffsetDateTime currentTime,
      OffsetDateTime expirationTime,
      SecretKey refreshKey) {
    return Jwts.builder()
        .setSubject(userId.value().toString())
        .setIssuedAt(Date.from(currentTime.toInstant()))
        .setExpiration(Date.from(expirationTime.toInstant()))
        .signWith(refreshKey, SignatureAlgorithm.HS256)
        .compact();
  }
}
