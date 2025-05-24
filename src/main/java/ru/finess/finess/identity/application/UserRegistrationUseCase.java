package ru.finess.finess.identity.application;

import com.github.sviperll.result4j.Result;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.finess.finess.common.application.UseCase;
import ru.finess.finess.identity.domain.User;
import ru.finess.finess.identity.domain.UserEncodedPassword;
import ru.finess.finess.identity.domain.UserFullName;
import ru.finess.finess.identity.domain.UserPassword;

@Service
@RequiredArgsConstructor
public class UserRegistrationUseCase
    implements UseCase<User, Void, UserRegistrationUseCase.Parameters> {

  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepository;

  public record Parameters(@NonNull UserPassword password, @NonNull UserFullName fullName) {}

  @Transactional
  @Override
  public Result<User, Void> execute(@NonNull Parameters parameters) {
    UserPassword password = parameters.password();
    UserEncodedPassword encodedPassword = passwordEncoder.encode(password);

    User user =
        User.builder().encodedPassword(encodedPassword).fullName(parameters.fullName()).build();
    userRepository.save(user);
    return Result.success(user);
  }
}
