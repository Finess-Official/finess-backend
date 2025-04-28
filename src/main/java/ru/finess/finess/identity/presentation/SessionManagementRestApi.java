package ru.finess.finess.identity.presentation;

import static ru.finess.finess.identity.application.UserAuthenticationUseCase.Error.InvalidPassword;
import static ru.finess.finess.identity.application.UserAuthenticationUseCase.Error.UserNotFound;

import com.github.sviperll.result4j.Result;
import java.time.OffsetDateTime;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.finess.finess.identity.application.SessionCreationUseCase;
import ru.finess.finess.identity.application.UserAuthenticationUseCase;
import ru.finess.finess.identity.application.UserRegistrationUseCase;
import ru.finess.finess.identity.domain.*;
import ru.finess.finess.identity.infrastructure.JwtValidator;
import ru.finess.finess.identity.presentation.api.SessionManagementApi;
import ru.finess.finess.identity.presentation.dto.SessionDto;
import ru.finess.finess.identity.presentation.dto.TokenRefreshParametersDto;
import ru.finess.finess.identity.presentation.dto.UserRegistrationParametersDto;
import ru.finess.finess.identity.presentation.dto.UserSigninParametersDto;

@Slf4j
@RestController
@RequestMapping("/api/identity")
@RequiredArgsConstructor
public class SessionManagementRestApi implements SessionManagementApi {

  private final ConversionService conversionService;
  private final UserRegistrationUseCase registrationUseCase;
  private final SessionCreationUseCase sessionCreationUseCase;
  private final UserAuthenticationUseCase userAuthenticationUseCase;
  private final JwtValidator jwtValidator;

  @Override
  public ResponseEntity<SessionDto> registerUser(
      UserRegistrationParametersDto userRegistrationParametersDto) {
    UserPassword password = new UserPassword(userRegistrationParametersDto.getPassword());

    return registerUser(password)
        .map(User::id)
        .flatMap(this::createSession)
        .map(this::toDto)
        .recoverError(error -> ResponseEntity.status(error).build());
  }

  @Override
  public ResponseEntity<SessionDto> signinUser(UserSigninParametersDto userSigninParametersDto) {
    UserId user = new UserId(userSigninParametersDto.getUserId());
    UserPassword password = new UserPassword(userSigninParametersDto.getPassword());

    return signinUser(user, password)
        .map(User::id)
        .flatMap(this::createSession)
        .map(this::toDto)
        .recoverError(error -> ResponseEntity.status(error).build());
  }

  @Override
  public ResponseEntity<SessionDto> refreshToken(TokenRefreshParametersDto tokenRefreshParametersDto) {
    String rawRefreshToken = tokenRefreshParametersDto.getRefreshToken();

    return jwtValidator.getSubject(rawRefreshToken)
        .map(userId -> createSession(userId)
        .map(this::toDto)
        .recoverError(error -> ResponseEntity.status(error).build()))
        .orElse(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
  }

  private Result<User, HttpStatus> registerUser(UserPassword password) {
    UserRegistrationUseCase.Parameters parameters =
        new UserRegistrationUseCase.Parameters(password);

    return registrationUseCase
        .execute(parameters)
        .mapError(ignored -> HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private Result<Session, HttpStatus> createSession(UserId userId) {
    SessionCreationUseCase.Parameters parameters =
        new SessionCreationUseCase.Parameters(userId, OffsetDateTime.now());
    return sessionCreationUseCase
        .execute(parameters)
        .mapError(ignored -> HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private ResponseEntity<SessionDto> toDto(Session session) {
    SessionDto dto = conversionService.convert(session, SessionDto.class);
    return ResponseEntity.ok(dto);
  }

  private Result<User, HttpStatus> signinUser(UserId userId, UserPassword password) {
    UserAuthenticationUseCase.Parameters parameters =
        new UserAuthenticationUseCase.Parameters(userId, password);
    return userAuthenticationUseCase
        .execute(parameters)
        .mapError(
            error ->
                switch (error) {
                  case UserNotFound ignored -> HttpStatus.NOT_FOUND;
                  case InvalidPassword ignored -> HttpStatus.UNAUTHORIZED;
                });
  }
}
