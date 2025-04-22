package ru.finess.finess.payment.domain;

import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;
import ru.finess.finess.identity.domain.UserId;
import ru.finess.finess.payment.application.AccountRepository;

@Component
public class AccountMother {

  private final AccountRepository accountRepository;

  public AccountMother(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  public Account createPersistedAccount(@NotNull UserId owner) {
    Account account = createAccount(owner);
    accountRepository.save(account);
    return account;
  }

  public static Account createAccount() {
    return createAccount(UserId.random());
  }

  public static Account createAccount(UserId owner) {
    return Account.builder()
        .ownerId(owner)
        .ownerName(new AccountOwnerName("Ivanov Ivan Ivanovich"))
        .inn(new AccountINN("1234567890"))
        .bik(new AccountBIK("123456789"))
        .number(new AccountNumber("12345678901234567890"))
        .build();
  }
}
