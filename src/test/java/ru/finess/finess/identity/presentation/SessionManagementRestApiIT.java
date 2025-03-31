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

@IntegrationTest
class SessionManagementRestApiIT {

  @Autowired MockMvc mockMvc;

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
}
