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
        .sessionToken(session.accessToken().value())
        .sessionExpiresAt(session.accessToken().expirationTime())
        // todo
        .refreshToken("00Fpzf4en68pCXTsMjcX8JPMctzN2Wiw4LDOBL_9pe")
        .refreshTokenExpiresAt(session.accessToken().expirationTime().plusDays(1))
        .attributes(new SessionAttributesDto().user(new UserDto().id(session.user().value())));
  }
}
