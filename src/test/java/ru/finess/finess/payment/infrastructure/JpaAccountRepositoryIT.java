package ru.finess.finess.payment.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.finess.finess.IntegrationTest;
import ru.finess.finess.identity.domain.UserId;
import ru.finess.finess.payment.domain.*;

@IntegrationTest
class JpaAccountRepositoryIT {

  @Autowired private JpaAccountRepository sut;

  @DisplayName("Check saving account")
  @Test
  void testSave() {
    // Arrange
    Account account = createAccount();

    // Act
    sut.save(account);

    // Assert
    assertTrue(sut.findById(account.id()).isPresent());
  }

  @DisplayName("Check loading account")
  @Test
  void testLoading() {
    // Arrange
    Account account = createAccount();

    sut.save(account);

    // Act
    Account actual = sut.findById(account.id()).orElseThrow();

    // Assert
    assertEquals(account, actual);
  }

  private static Account createAccount() {
    return Account.builder()
        .ownerId(UserId.random())
        .ownerName(new AccountOwnerName("Ivanov Ivan Ivanovich"))
        .inn(new AccountINN("1234567890"))
        .bik(new AccountBIK("123456789"))
        .number(new AccountNumber("12345678901234567890"))
        .build();
  }
}
