package ru.finess.finess.identity.presentation;

import static ru.finess.finess.identity.application.UserAuthenticationUseCase.AuthenticationError.*;

import com.github.sviperll.result4j.Result;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.finess.finess.identity.application.Session;
import ru.finess.finess.identity.application.SessionCreationUseCase;
import ru.finess.finess.identity.application.UserAuthenticationUseCase;
import ru.finess.finess.identity.application.UserRegistrationUseCase;
import ru.finess.finess.identity.domain.User;
import ru.finess.finess.identity.domain.UserId;
import ru.finess.finess.identity.domain.UserPassword;
import ru.finess.finess.identity.presentation.api.SessionManagementApi;
import ru.finess.finess.identity.presentation.dto.SessionDto;
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

  @Override
  public ResponseEntity<SessionDto> registerUser(
      UserRegistrationParametersDto userRegistrationParametersDto) {
    UserPassword password = new UserPassword(userRegistrationParametersDto.getPassword());

    return registerUser(password)
        .map(this::createSession)
        .map(this::toDto)
        .recoverError(status -> ResponseEntity.status(status).build());
  }

  @Override
  public ResponseEntity<SessionDto> signinUser(UserSigninParametersDto userSigninParametersDto) {
    UserId user = new UserId(userSigninParametersDto.getUserId());
    UserPassword password = new UserPassword(userSigninParametersDto.getPassword());

    return signinUser(user, password)
        .map(this::createSession)
        .map(this::toDto)
        .recoverError(error -> ResponseEntity.status(error).build());
  }

  private Result<User, HttpStatus> registerUser(UserPassword password) {
    UserRegistrationUseCase.Parameters parameters =
        new UserRegistrationUseCase.Parameters(password);

    return registrationUseCase
        .execute(parameters)
        .mapError(ignored -> HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private Session createSession(User user) {
    SessionCreationUseCase.Parameters parameters =
        new SessionCreationUseCase.Parameters(user.id(), OffsetDateTime.now());
    return sessionCreationUseCase
        .execute(parameters)
        .orOnErrorThrow(error -> new RuntimeException(error.toString()));
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
            error -> {
              log.error("Error during user authentication: {}", error);
              return switch (error) {
                case UserNotFound userNotFound -> HttpStatus.NOT_FOUND;
                case InvalidPassword invalidPassword -> HttpStatus.UNAUTHORIZED;
              };
            });
  }
}
