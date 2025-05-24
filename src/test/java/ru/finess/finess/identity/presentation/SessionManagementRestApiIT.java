package ru.finess.finess.identity.presentation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import ru.finess.finess.IntegrationTest;
import ru.finess.finess.common.UserMother;
import ru.finess.finess.identity.application.SessionCreationUseCase;
import ru.finess.finess.identity.domain.Session;
import ru.finess.finess.identity.domain.User;
import ru.finess.finess.identity.domain.UserId;
import ru.finess.finess.identity.domain.UserPassword;

@IntegrationTest
class SessionManagementRestApiIT {

  @Autowired MockMvc mockMvc;

  @Autowired UserMother userMother;

  @Autowired private SessionCreationUseCase sessionCreationUseCase;

  @DisplayName("Check user authenticated")
  @Test
  void registerUser() throws Exception {
    // Arrange
    String requestBody =
        """
            {
                "password": "Example1",
                "firstName": "Example",
                "lastName": "Example",
                "middleName": "Example"
            }
        """;

    // Act
    ResultActions actual =
        mockMvc.perform(
            post("/api/identity/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

    // Assert
    expectTokensExists(actual)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.attributes.user.id").exists());
  }

  @DisplayName("Check sign in an existing user successfully")
  @Test
  void signinUser() throws Exception {
    // Arrange
    UserPassword password = new UserPassword("Example1");
    User user = userMother.create(password);

    String requestBody =
        """
                {
                    "userId": "%s",
                    "password": "%s"
                }
            """
            .formatted(user.id().value(), password.value());

    // Act
    ResultActions actual =
        mockMvc.perform(
            post("/api/identity/v1/tokens")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody));

    // Assert
    expectTokensExists(actual)
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.attributes.user.id").value(user.id().value().toString()));
  }

  @DisplayName("Check sign in an non existing user")
  @Test
  void signinUserNotFound() throws Exception {
    // Arrange
    UserPassword password = new UserPassword("Example1");
    userMother.create(password);

    String requestBody =
        """
                    {
                        "userId": "%s",
                        "password": "%s"
                    }
                """
            .formatted(UserId.random().value(), password.value());

    // Act & Assert
    mockMvc
        .perform(
            post("/api/identity/v1/tokens")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isNotFound());
  }

  @DisplayName("Check sign in an existing user with wrong password fails")
  @Test
  void signinUserUnauthorized() throws Exception {
    // Arrange
    UserPassword password = new UserPassword("Example1");
    User user = userMother.create(password);

    String requestBody =
        """
                        {
                            "userId": "%s",
                            "password": "%s"
                        }
                    """
            .formatted(user.id().value(), new UserPassword("SomeExample2"));

    // Act & Assert
    mockMvc
        .perform(
            post("/api/identity/v1/tokens")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isUnauthorized());
  }

  @DisplayName("Check token refreshed")
  @Test
  void getRefreshToken() throws Exception {
    // Arrange
    User user = userMother.create();
    Session session =
        sessionCreationUseCase
            .execute(new SessionCreationUseCase.Parameters(user.id(), OffsetDateTime.now()))
            .orOnErrorThrow(
                ignored -> new RuntimeException("Failed to create session" + user.id()));

    String requestBody =
        """
            {
              "refreshToken": "%s"
            }
            """
            .formatted(session.refreshToken().value());

    // Act & Assert
    ResultActions resultActions =
        mockMvc
            .perform(
                post("/api/identity/v1/tokens:refresh")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
            .andDo(print())
            .andExpect(status().isOk());

    expectTokensExists(resultActions);
  }

  @DisplayName("Check invalid token")
  @Test
  void invalidRefreshToken() throws Exception {
    // Arrange
    String requestBody =
        """
            {
              "refreshToken": "%s"
            }
            """
            .formatted("12345dadsjkds");

    // Act & Assert
    mockMvc
        .perform(
            post("/api/identity/v1/tokens:refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andDo(print())
        .andExpect(status().isUnauthorized());
  }

  private static ResultActions expectTokensExists(ResultActions actual) throws Exception {
    return actual
        .andExpect(jsonPath("$.sessionToken").exists())
        .andExpect(jsonPath("$.sessionExpiresAt").exists())
        .andExpect(jsonPath("$.refreshToken").exists())
        .andExpect(jsonPath("$.refreshTokenExpiresAt").exists());
  }
}
