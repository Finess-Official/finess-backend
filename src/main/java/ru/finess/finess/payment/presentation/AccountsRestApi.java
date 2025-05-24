package ru.finess.finess.payment.presentation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.finess.finess.identity.domain.UserId;
import ru.finess.finess.payment.application.AccountCreationUseCase;
import ru.finess.finess.payment.application.DeletingAccountUseCase;
import ru.finess.finess.payment.application.GettingAccountUseCase;
import ru.finess.finess.payment.application.GettingAccountsUseCase;
import ru.finess.finess.payment.domain.*;
import ru.finess.finess.payment.presentation.api.AccountsApi;
import ru.finess.finess.payment.presentation.dto.AccountCreationParametersDto;
import ru.finess.finess.payment.presentation.dto.AccountDto;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class AccountsRestApi implements AccountsApi {

  private final AccountCreationUseCase creationUseCase;
  private final GettingAccountUseCase gettingAccountUseCase;
  private final GettingAccountsUseCase gettingAccountsUseCase;
  private final DeletingAccountUseCase deletingAccountUseCase;
  private final Supplier<Optional<UserId>> currentUserSupplier;
  private final ConversionService conversionService;

  @Override
  public ResponseEntity<AccountDto> createAccount(
      AccountCreationParametersDto accountCreationParametersDto) {
    AccountNumber accountNumber =
        new AccountNumber(accountCreationParametersDto.getAccountNumber());
    AccountBIK bik = new AccountBIK(accountCreationParametersDto.getBik());
    AccountINN inn = new AccountINN(accountCreationParametersDto.getInn());
    AccountOwnerName name = new AccountOwnerName(accountCreationParametersDto.getOwnerName());

    UserId currentUser = getCurrentUser();

    AccountCreationUseCase.Parameters parameters =
        new AccountCreationUseCase.Parameters(currentUser, accountNumber, bik, inn, name);
    return creationUseCase
        .execute(parameters)
        .map(this::toDto)
        .map(ResponseEntity::ok)
        .recoverError(e -> ResponseEntity.badRequest().build());
  }

  @Override
  public ResponseEntity<AccountDto> getAccount(UUID id) {
    AccountId accountId = new AccountId(id);
    return gettingAccountUseCase
        .execute(accountId)
        .map(this::toDto)
        .map(ResponseEntity::ok)
        .recoverError(e -> ResponseEntity.notFound().build());
  }

  @Override
  public ResponseEntity<List<AccountDto>> getAccounts() {
    UserId currentUser = getCurrentUser();
    List<AccountDto> dtos =
        gettingAccountsUseCase
            .execute(new GettingAccountsUseCase.Parameters(currentUser))
            .map(List::stream)
            .recoverError(ignored -> Stream.empty())
            .map(this::toDto)
            .toList();
    return ResponseEntity.ok(dtos);
  }

  @Override
  public ResponseEntity<Void> deleteAccount(UUID id) {
    AccountId accountId = new AccountId(id);
    UserId currentUser = getCurrentUser();
    return deletingAccountUseCase
        .execute(new DeletingAccountUseCase.Parameters(currentUser, accountId))
        .map(ignored -> ResponseEntity.noContent().<Void>build())
        .recoverError(ignored -> ResponseEntity.notFound().build());
  }

  private UserId getCurrentUser() {
    return currentUserSupplier
        .get()
        .orElseThrow(() -> new SecurityException("Current user is not authentic"));
  }

  private AccountDto toDto(Account account) {
    return conversionService.convert(account, AccountDto.class);
  }
}
