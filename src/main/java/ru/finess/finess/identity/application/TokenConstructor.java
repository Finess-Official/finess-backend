package ru.finess.finess.identity.application;

import java.time.OffsetDateTime;
import lombok.NonNull;
import ru.finess.finess.identity.domain.UserId;

public interface TokenConstructor {

  SessionToken createToken(@NonNull UserId userId, @NonNull OffsetDateTime currentTime);
}
