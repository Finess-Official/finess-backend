package ru.finess.finess.identity.application;

import com.github.sviperll.result4j.Result;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.finess.finess.common.application.TransactionWrapper;
import ru.finess.finess.common.application.UseCase;
import ru.finess.finess.identity.domain.User;
import ru.finess.finess.identity.domain.UserId;
import ru.finess.finess.identity.domain.UserPassword;

@Service
@RequiredArgsConstructor
public class UserAuthenticationUseCase
    implements UseCase<
        User, UserAuthenticationUseCase.AuthenticationError, UserAuthenticationUseCase.Parameters> {

  public record Parameters(@NonNull UserId user, @NonNull UserPassword password) {}

  public sealed interface AuthenticationError
      permits AuthenticationError.UserNotFound, AuthenticationError.InvalidPassword {
    record UserNotFound(UserId userId) implements AuthenticationError {}

    record InvalidPassword(UserId userId) implements AuthenticationError {}
  }

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final TransactionWrapper transactionWrapper;

  @Override
  public Result<User, AuthenticationError> execute(@NonNull Parameters parameters) {
    UserId userId = parameters.user();
    UserPassword password = parameters.password();

    return transactionWrapper.execute(
        () ->
            userRepository
                .find(userId)
                .map(
                    user -> {
                      if (passwordEncoder.matches(password, user.hashedPassword())) {
                        return Result.<User, AuthenticationError>success(user);
                      } else {
                        return Result.<User, AuthenticationError>error(
                            new AuthenticationError.InvalidPassword(userId));
                      }
                    })
                .orElseGet(() -> Result.error(new AuthenticationError.UserNotFound(userId))));
  }
}
