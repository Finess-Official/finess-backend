package ru.finess.finess.identity.application;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.github.sviperll.result4j.Result;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.finess.finess.IntegrationTest;
import ru.finess.finess.common.UserMother;
import ru.finess.finess.identity.domain.Session;
import ru.finess.finess.identity.domain.User;
import ru.finess.finess.identity.domain.UserId;

@IntegrationTest
class SessionCreationUseCaseIT {

  @Autowired SessionCreationUseCase sut;

  @Autowired UserMother userMother;

  @DisplayName("Check session creation")
  @Test
  void testCreation() {
    // Arrange
    User user = userMother.create();
    SessionCreationUseCase.Parameters parameters =
        new SessionCreationUseCase.Parameters(user.id(), OffsetDateTime.now());

    // Act
    Result<Session, SessionCreationUseCase.UserNotFoundError> actual = sut.execute(parameters);

    // Assert
    assertFalse(actual.isError());
  }

  @DisplayName("Check session not created if user not found")
  @Test
  void testCreationNonExisting() {
    // Arrange
    SessionCreationUseCase.Parameters parameters =
        new SessionCreationUseCase.Parameters(new UserId(UUID.randomUUID()), OffsetDateTime.now());

    // Act
    Result<Session, SessionCreationUseCase.UserNotFoundError> actual = sut.execute(parameters);

    // Assert
    assertEquals(
        actual, Result.error(new SessionCreationUseCase.UserNotFoundError(parameters.userId())));
  }
}
