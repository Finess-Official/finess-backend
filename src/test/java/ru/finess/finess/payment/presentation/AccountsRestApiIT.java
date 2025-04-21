package ru.finess.finess.payment.presentation;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.OffsetDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.finess.finess.IntegrationTest;
import ru.finess.finess.common.UserMother;
import ru.finess.finess.identity.application.SessionCreationUseCase;
import ru.finess.finess.identity.domain.Session;
import ru.finess.finess.identity.domain.User;

@IntegrationTest
class AccountsRestApiIT {

  @Autowired private MockMvc mockMvc;

  @Autowired private UserMother userMother;

  @Autowired private SessionCreationUseCase sessionCreationUseCase;

  @DisplayName("Check account creation")
  @Test
  void testCreation() throws Exception {
    // Arrange
    User user = userMother.create();
    Session session =
        sessionCreationUseCase
            .execute(new SessionCreationUseCase.Parameters(user.id(), OffsetDateTime.now()))
            .orOnErrorThrow(e -> new RuntimeException());

    String name = "Иванов Иван Иванович";
    String inn = "123456789012";
    String bik = "123456789";
    String number = "12345678901234567890";
    String body =
        """
        {
          "ownerName": "%s",
          "inn": "%s",
          "bik": "%s",
          "accountNumber": "%s"
        }"""
            .formatted(name, inn, bik, number);

    // Act + Assert
    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/payment/v1/accounts")
                .content(body)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + session.accessToken().value()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").isString())
        .andExpect(jsonPath("$.ownerName").value(name))
        .andExpect(jsonPath("$.inn").value(inn))
        .andExpect(jsonPath("$.bik").value(bik))
        .andExpect(jsonPath("$.accountNumber").value(number));
  }
}
