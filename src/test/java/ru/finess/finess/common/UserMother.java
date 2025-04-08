package ru.finess.finess.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.testcontainers.shaded.org.checkerframework.checker.nullness.qual.NonNull;
import ru.finess.finess.identity.application.UserRegistrationUseCase;
import ru.finess.finess.identity.domain.User;
import ru.finess.finess.identity.domain.UserPassword;

@Component
public class UserMother {

  @Autowired private UserRegistrationUseCase userRegistrationUseCase;

  public User create() {
    UserPassword password = new UserPassword("SomePassword21");
    return create(password);
  }

  public User create(@NonNull UserPassword password) {
    return userRegistrationUseCase
        .execute(new UserRegistrationUseCase.Parameters(password))
        .orOnErrorThrow(ignored -> new RuntimeException());
  }
}
