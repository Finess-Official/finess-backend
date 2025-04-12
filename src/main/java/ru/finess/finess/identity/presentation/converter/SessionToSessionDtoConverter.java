package ru.finess.finess.identity.presentation.converter;

import java.util.Objects;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.finess.finess.identity.application.Session;
import ru.finess.finess.identity.presentation.dto.SessionAttributesDto;
import ru.finess.finess.identity.presentation.dto.SessionDto;
import ru.finess.finess.identity.presentation.dto.UserDto;

@Component
public class SessionToSessionDtoConverter implements Converter<Session, SessionDto> {

  @Override
  public SessionDto convert(Session session) {
    return new SessionDto()
        .sessionToken(session.accessToken().value())
        .sessionExpiresAt(session.accessToken().expirationTime())
        .refreshToken(Objects.requireNonNull(session.refreshToken()).value())
        .refreshTokenExpiresAt(Objects.requireNonNull(session.refreshToken()).expirationTime())
        .attributes(new SessionAttributesDto().user(new UserDto().id(session.user().value())));
  }
}
