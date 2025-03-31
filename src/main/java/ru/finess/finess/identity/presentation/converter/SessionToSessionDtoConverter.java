package ru.finess.finess.identity.presentation.converter;

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
        .sessionToken(session.token().value())
        .sessionExpiresAt(session.token().expirationTime())
        .attributes(new SessionAttributesDto().user(new UserDto().id(session.user().value())));
  }
}
