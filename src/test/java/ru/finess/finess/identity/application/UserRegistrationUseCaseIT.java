package ru.finess.finess.identity.application;

import static org.junit.jupiter.api.Assertions.assertFalse;

import com.github.sviperll.result4j.Result;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.finess.finess.IntegrationTest;
import ru.finess.finess.identity.domain.User;
import ru.finess.finess.identity.domain.UserPassword;

@IntegrationTest
class UserRegistrationUseCaseIT {

  @Autowired UserRegistrationUseCase sut;

  @DisplayName("Check user registered")
  @Test
  void testRegistration() {
    // Arrange
    UserPassword password = new UserPassword("SomePassword21");
    UserRegistrationUseCase.Parameters parameters =
        new UserRegistrationUseCase.Parameters(password);

    // Act
    Result<User, Void> actual = sut.execute(parameters);

    // Assert
    assertFalse(actual.isError());
  }
}
