package ru.finess.finess.payment.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.finess.finess.IntegrationTest;
import ru.finess.finess.payment.domain.Account;
import ru.finess.finess.payment.domain.AccountMother;

@IntegrationTest
class JpaAccountRepositoryIT {

  @Autowired private JpaAccountRepository sut;

  @DisplayName("Check saving account")
  @Test
  void testSave() {
    // Arrange
    Account account = AccountMother.createAccount();

    // Act
    sut.save(account);

    // Assert
    assertTrue(sut.findById(account.id()).isPresent());
  }

  @DisplayName("Check loading account")
  @Test
  void testLoading() {
    // Arrange
    Account account = AccountMother.createAccount();

    sut.save(account);

    // Act
    Account actual = sut.findById(account.id()).orElseThrow();

    // Assert
    assertEquals(account, actual);
  }
}
