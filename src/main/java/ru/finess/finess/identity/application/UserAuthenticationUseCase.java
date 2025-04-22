package ru.finess.finess.identity.application;

import com.github.sviperll.result4j.Result;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.finess.finess.common.application.UseCase;
import ru.finess.finess.identity.domain.User;
import ru.finess.finess.identity.domain.UserId;
import ru.finess.finess.identity.domain.UserPassword;

@Service
@RequiredArgsConstructor
public class UserAuthenticationUseCase
    implements UseCase<
        User, UserAuthenticationUseCase.Error, UserAuthenticationUseCase.Parameters> {

  public record Parameters(@NonNull UserId user, @NonNull UserPassword password) {}

  public sealed interface Error permits Error.UserNotFound, Error.InvalidPassword {

    record UserNotFound(UserId userId) implements Error {}

    record InvalidPassword(UserId userId) implements Error {}
  }

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Transactional(readOnly = true)
  @Override
  public Result<User, Error> execute(@NonNull Parameters parameters) {
    UserId userId = parameters.user();
    UserPassword password = parameters.password();

    return userRepository
        .findById(userId)
        .map(user -> authenticateUser(user, password))
        .orElseGet(() -> Result.error(new Error.UserNotFound(userId)));
  }

  private Result<User, Error> authenticateUser(User user, UserPassword password) {
    if (passwordEncoder.matches(password, user.hashedPassword())) {
      return Result.success(user);
    } else {
      return Result.error(new Error.InvalidPassword(user.id()));
    }
  }
}
