package ru.finess.finess.payment.presentation;

import java.util.Optional;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.finess.finess.identity.domain.UserId;
import ru.finess.finess.payment.application.AccountCreationUseCase;
import ru.finess.finess.payment.domain.AccountBIK;
import ru.finess.finess.payment.domain.AccountINN;
import ru.finess.finess.payment.domain.AccountNumber;
import ru.finess.finess.payment.domain.AccountOwnerName;
import ru.finess.finess.payment.presentation.api.AccountsApi;
import ru.finess.finess.payment.presentation.dto.AccountCreationParametersDto;
import ru.finess.finess.payment.presentation.dto.AccountDto;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class AccountsRestApi implements AccountsApi {

  private final AccountCreationUseCase creationUseCase;
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

    UserId currentUser =
        currentUserSupplier
            .get()
            .orElseThrow(() -> new SecurityException("Current user is not authentic"));

    AccountCreationUseCase.Parameters parameters =
        new AccountCreationUseCase.Parameters(currentUser, accountNumber, bik, inn, name);
    return creationUseCase
        .execute(parameters)
        .map(account -> conversionService.convert(account, AccountDto.class))
        .map(ResponseEntity::ok)
        .recoverError(e -> ResponseEntity.badRequest().build());
  }
}
