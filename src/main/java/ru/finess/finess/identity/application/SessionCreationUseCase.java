package ru.finess.finess.identity.application;

import com.github.sviperll.result4j.Result;
import java.time.OffsetDateTime;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.finess.finess.common.application.UseCase;
import ru.finess.finess.identity.domain.UserId;

@Service
@RequiredArgsConstructor
public class SessionCreationUseCase
    implements UseCase<
        Session, SessionCreationUseCase.UserNotFoundError, SessionCreationUseCase.Parameters> {

  private final TokenConstructor tokenConstructor;
  private final UserRepository userRepository;

  public record Parameters(@NonNull UserId userId, @NonNull OffsetDateTime currentTime) {}

  public record UserNotFoundError(@NonNull UserId userId) {}

  @Override
  public Result<Session, UserNotFoundError> execute(@NonNull Parameters parameters) {
    UserId userId = parameters.userId();
    if (userRepository.exists(userId)) {
      SessionToken accessToken =
          tokenConstructor.createAccessToken(userId, parameters.currentTime());
      SessionToken refreshToken =
          tokenConstructor.createRefreshToken(userId, parameters.currentTime());
      return Result.success(
          Session.builder()
              .user(userId)
              .accessToken(accessToken)
              .refreshToken(refreshToken)
              .build());
    } else {
      return Result.error(new UserNotFoundError(userId));
    }
  }
}
