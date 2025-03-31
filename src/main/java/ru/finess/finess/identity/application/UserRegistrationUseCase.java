package ru.finess.finess.identity.application;

import com.github.sviperll.result4j.Result;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.finess.finess.common.application.TransactionWrapper;
import ru.finess.finess.common.application.UseCase;
import ru.finess.finess.identity.domain.User;
import ru.finess.finess.identity.domain.UserEncodedPassword;
import ru.finess.finess.identity.domain.UserPassword;

@Service
@RequiredArgsConstructor
public class UserRegistrationUseCase
    implements UseCase<User, Void, UserRegistrationUseCase.Parameters> {

  private final PasswordEncoder passwordEncoder;
  private final TransactionWrapper transactionWrapper;
  private final UserRepository userRepository;

  public record Parameters(@NonNull UserPassword password) {}

  @Override
  public Result<User, Void> execute(@NonNull Parameters parameters) {
    User createdUser =
        transactionWrapper.execute(
            () -> {
              UserPassword password = parameters.password();
              UserEncodedPassword encodedPassword = passwordEncoder.encode(password);

              User user = User.builder().encodedPassword(encodedPassword).build();
              userRepository.save(user);
              return user;
            });
    return Result.success(createdUser);
  }
}
