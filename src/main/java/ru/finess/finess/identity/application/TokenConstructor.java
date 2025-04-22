package ru.finess.finess.identity.application;

import java.time.OffsetDateTime;
import lombok.NonNull;
import ru.finess.finess.identity.domain.SessionToken;
import ru.finess.finess.identity.domain.UserId;

public interface TokenConstructor {

  SessionToken createAccessToken(@NonNull UserId userId, @NonNull OffsetDateTime currentTime);

  SessionToken createRefreshToken(@NonNull UserId userId, @NonNull OffsetDateTime currentTime);
}
