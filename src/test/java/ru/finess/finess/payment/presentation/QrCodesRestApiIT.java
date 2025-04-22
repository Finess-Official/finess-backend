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
import ru.finess.finess.payment.domain.Account;
import ru.finess.finess.payment.domain.AccountId;
import ru.finess.finess.payment.domain.AccountMother;

@IntegrationTest
class QrCodesRestApiIT {

  @Autowired MockMvc mockMvc;

  @Autowired AccountMother accountMother;

  @Autowired UserMother userMother;

  @Autowired SessionCreationUseCase sessionCreationUseCase;

  @DisplayName("Check qrcode creation")
  @Test
  void testCreation() throws Exception {
    // Arrange
    User user = userMother.create();
    Account account = accountMother.createPersistedAccount(user.id());
    Session session =
        sessionCreationUseCase
            .execute(new SessionCreationUseCase.Parameters(user.id(), OffsetDateTime.now()))
            .orOnErrorThrow(e -> new RuntimeException());

    String requestBody =
        """
        {
          "accountId": "%s",
          "amount": 100.50
        }
        """
            .formatted(account.id().id());

    // Act + Assert
    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/payment/v1/qrcodes")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + session.accessToken().value()))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").isString())
        .andExpect(jsonPath("$.accountId").value(account.id().id().toString()))
        .andExpect(jsonPath("$.amount").value("100.5"));
  }

  @DisplayName("Check qrcode creation with incorrect amount")
  @Test
  void testAmountIsIncorrect() throws Exception {
    // Arrange
    User user = userMother.create();
    Account account = accountMother.createPersistedAccount(user.id());
    Session session =
        sessionCreationUseCase
            .execute(new SessionCreationUseCase.Parameters(user.id(), OffsetDateTime.now()))
            .orOnErrorThrow(e -> new RuntimeException());

    String requestBody =
        """
        {
          "accountId": "%s",
          "amount": 100.501
        }
        """
            .formatted(account.id().id());

    // Act + Assert
    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/payment/v1/qrcodes")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + session.accessToken().value()))
        .andDo(print())
        .andExpect(status().isBadRequest());
  }

  @DisplayName("Check qrcode creation with non existing account")
  @Test
  void testAccountNotFound() throws Exception {
    // Arrange
    User user = userMother.create();
    accountMother.createPersistedAccount(user.id());
    Session session =
        sessionCreationUseCase
            .execute(new SessionCreationUseCase.Parameters(user.id(), OffsetDateTime.now()))
            .orOnErrorThrow(e -> new RuntimeException());

    String requestBody =
        """
        {
          "accountId": "%s",
          "amount": 100.50
        }
        """
            .formatted(AccountId.random().id());

    // Act + Assert
    mockMvc
        .perform(
            MockMvcRequestBuilders.post("/api/payment/v1/qrcodes")
                .content(requestBody)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + session.accessToken().value()))
        .andDo(print())
        .andExpect(status().isNotFound());
  }
}
