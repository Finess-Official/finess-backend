package ru.finess.finess.identity.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.github.sviperll.result4j.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.finess.finess.IntegrationTest;
import ru.finess.finess.common.UserMother;
import ru.finess.finess.identity.application.UserAuthenticationUseCase.Error;
import ru.finess.finess.identity.domain.User;
import ru.finess.finess.identity.domain.UserId;
import ru.finess.finess.identity.domain.UserPassword;

@IntegrationTest
class UserAuthenticationUseCaseIT {

  @Autowired UserAuthenticationUseCase sut;

  @Autowired UserMother userMother;

  @DisplayName("Check user authenticated")
  @Test
  void testAuthentication() {
    // Arrange
    UserPassword password = new UserPassword("SomePassword21");
    User user = userMother.create(password);
    UserAuthenticationUseCase.Parameters parameters =
        new UserAuthenticationUseCase.Parameters(user.id(), password);

    // Act
    Result<User, Error> result = sut.execute(parameters);

    // Assert
    assertFalse(result.isError());
  }

  @DisplayName("Check user not authenticated")
  @Test
  void testNotAuthenticated() {
    // Arrange
    UserPassword password = new UserPassword("SomePassword21");
    User user = userMother.create(password);
    UserAuthenticationUseCase.Parameters parameters =
        new UserAuthenticationUseCase.Parameters(
            user.id(), new UserPassword("SomeWrongPassword21"));

    // Act
    Result<User, Error> result = sut.execute(parameters);

    // Assert
    assertEquals(result, Result.error(new Error.InvalidPassword(user.id())));
  }

  @DisplayName("Check user not found")
  @Test
  void testNotFound() {
    // Arrange
    User user = userMother.create();
    UserId random = UserId.random();
    UserAuthenticationUseCase.Parameters parameters =
        new UserAuthenticationUseCase.Parameters(random, new UserPassword("SomePwd21"));

    // Act
    Result<User, Error> result = sut.execute(parameters);

    // Assert
    assertEquals(result, Result.error(new Error.UserNotFound(random)));
  }
}
