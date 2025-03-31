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
import ru.finess.finess.identity.application.SessionToken;
import ru.finess.finess.identity.application.TokenConstructor;
import ru.finess.finess.identity.domain.UserId;

@Component
public class JwtTokenConstructor implements TokenConstructor {

  private final Duration expirationDuration;
  private final SecretKey key;

  public JwtTokenConstructor(
      @Value("${session.jwt.access.key}") String secretKey,
      @Value("${session.jwt.access.expirationDuration}") Duration expirationDuration) {
    this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    this.expirationDuration = expirationDuration;
  }

  @Override
  public SessionToken createToken(@NonNull UserId userId, @NonNull OffsetDateTime currentTime) {
    OffsetDateTime expirationTime = currentTime.plus(this.expirationDuration);
    String token =
        Jwts.builder()
            .setSubject(userId.value().toString())
            .setIssuedAt(Date.from(currentTime.toInstant()))
            .setExpiration(Date.from(expirationTime.toInstant()))
            .signWith(key, SignatureAlgorithm.HS256)
            .compact();
    return new SessionToken(token, expirationTime);
  }
}
