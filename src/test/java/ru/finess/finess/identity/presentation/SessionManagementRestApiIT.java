package ru.finess.finess.identity.presentation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.finess.finess.IntegrationTest;
import ru.finess.finess.common.UserMother;
import ru.finess.finess.identity.domain.User;
import ru.finess.finess.identity.domain.UserId;
import ru.finess.finess.identity.domain.UserPassword;

@IntegrationTest
class SessionManagementRestApiIT {

  @Autowired MockMvc mockMvc;

  @Autowired UserMother userMother;

  @DisplayName("Check user authenticated")
  @Test
  void registerUser() throws Exception {
    // Arrange
    String requestBody =
        """
            {
                "password": "Example1"
            }
        """;

    // Act & Assert
    mockMvc
        .perform(
            post("/api/identity/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.sessionToken").exists())
        .andExpect(jsonPath("$.sessionExpiresAt").exists())
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

    // Act & Assert
    mockMvc
        .perform(
            post("/api/identity/v1/tokens")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.sessionToken").exists())
        .andExpect(jsonPath("$.sessionExpiresAt").exists())
        .andExpect(jsonPath("$.attributes.user.id").value(user.id().value().toString()));
  }

  @DisplayName("Check sign in an non existing user")
  @Test
  void signinUserNotFound() throws Exception {
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
}
