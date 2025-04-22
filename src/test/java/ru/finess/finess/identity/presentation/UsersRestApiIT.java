package ru.finess.finess.identity.presentation;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import ru.finess.finess.IntegrationTest;
import ru.finess.finess.common.UserMother;
import ru.finess.finess.identity.application.SessionCreationUseCase;
import ru.finess.finess.identity.domain.Session;
import ru.finess.finess.identity.domain.User;

@IntegrationTest
class UsersRestApiIT {

  @Autowired private MockMvc mockMvc;

  @Autowired private UserMother userMother;

  @Autowired private SessionCreationUseCase sessionCreationUseCase;

  @DisplayName("Current user should be returned")
  @Test
  void testGetCurrentUser() throws Exception {
    // Arrange
    User user = userMother.create();
    Session session =
        sessionCreationUseCase
            .execute(new SessionCreationUseCase.Parameters(user.id(), OffsetDateTime.now()))
            .orOnErrorThrow(
                ignored -> new RuntimeException("Failed to create session" + user.id()));

    // Act
    mockMvc
        .perform(
            get("/api/identity/v1/current")
                .header("Authorization", "Bearer " + session.accessToken().value()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(user.id().value().toString()));
  }

  @DisplayName("Check unauthorized access to current user")
  @Test
  void testGetCurrentUnauthorized() throws Exception {
    // Arrange
    userMother.create();

    // Act
    mockMvc.perform(get("/api/identity/v1/current")).andExpect(status().isUnauthorized());
  }

  @DisplayName("Check malformed JWT token")
  @Test
  void testGetCurrentMalformedJwt() throws Exception {
    // Arrange
    User user = userMother.create();
    Session session =
        sessionCreationUseCase
            .execute(new SessionCreationUseCase.Parameters(user.id(), OffsetDateTime.now()))
            .orOnErrorThrow(
                ignored -> new RuntimeException("Failed to create session" + user.id()));

    // Act
    mockMvc
        .perform(
            get("/api/identity/v1/current")
                .header("Authorization", "Bearer " + session.accessToken().value() + "malformed"))
        .andExpect(status().isUnauthorized());
  }

  @DisplayName("Check expired JWT token")
  @Test
  void testGetCurrentExpiredJwt() throws Exception {
    // Arrange
    User user = userMother.create();
    Session session =
        sessionCreationUseCase
            .execute(
                new SessionCreationUseCase.Parameters(
                    user.id(), OffsetDateTime.now().minusMinutes(35)))
            .orOnErrorThrow(
                ignored -> new RuntimeException("Failed to create session" + user.id()));

    // Act
    mockMvc
        .perform(
            get("/api/identity/v1/current")
                .header("Authorization", "Bearer " + session.accessToken().value()))
        .andExpect(status().isUnauthorized());
  }
}
