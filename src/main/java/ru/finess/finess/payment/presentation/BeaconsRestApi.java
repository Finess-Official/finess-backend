package ru.finess.finess.payment.presentation;

import com.github.sviperll.result4j.Result;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.finess.finess.identity.domain.UserId;
import ru.finess.finess.payment.application.AccountCreationUseCase;
import ru.finess.finess.payment.application.PaymentBeaconCreationUseCase;
import ru.finess.finess.payment.domain.Account;
import ru.finess.finess.payment.domain.AccountBIK;
import ru.finess.finess.payment.domain.AccountINN;
import ru.finess.finess.payment.domain.AccountId;
import ru.finess.finess.payment.domain.AccountNumber;
import ru.finess.finess.payment.domain.AccountOwnerName;
import ru.finess.finess.payment.domain.PaymentAmount;
import ru.finess.finess.payment.domain.PaymentBeacon;
import ru.finess.finess.payment.presentation.api.BeaconsApi;
import ru.finess.finess.payment.presentation.dto.AccountCreationParametersDto;
import ru.finess.finess.payment.presentation.dto.BeaconConfigurationDto;
import ru.finess.finess.payment.presentation.dto.BeaconCreationRequestDto;
import ru.finess.finess.payment.presentation.dto.BeaconInfoDto;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class BeaconsRestApi implements BeaconsApi {

  private final ConversionService conversionService;
  private final AccountCreationUseCase accountCreationUseCase;
  private final PaymentBeaconCreationUseCase beaconCreationUseCase;
  private final Supplier<Optional<UserId>> currentUserSupplier;

  @Override
  public ResponseEntity<BeaconInfoDto> createBeacon(BeaconCreationRequestDto parameters) {
    UserId currentUser =
        currentUserSupplier
            .get()
            .orElseThrow(() -> new SecurityException("Current user is not authenticated"));
    PaymentAmount amount = new PaymentAmount(BigDecimal.valueOf(parameters.getAmount()));

    return createAccount(parameters, currentUser)
        .flatMap(account -> createPaymentBeacon(account.id(), amount, currentUser))
        .map(this::toDto)
        .map(ResponseEntity::ok)
        .recoverError(status -> ResponseEntity.status(status).build());
  }

  @Override
  public ResponseEntity<Void> deactivateBeacon(Integer major, Integer minor) {
    return BeaconsApi.super.deactivateBeacon(major, minor);
  }

  @Override
  public ResponseEntity<BeaconInfoDto> findBeaconPaymentInfo(Integer major, Integer minor) {
    return BeaconsApi.super.findBeaconPaymentInfo(major, minor);
  }

  @Override
  public ResponseEntity<BeaconConfigurationDto> getBeaconConfiguration() {
    return BeaconsApi.super.getBeaconConfiguration();
  }

  private Result<Account, HttpStatus> createAccount(
      BeaconCreationRequestDto parametersDto, UserId currentUser) {
    AccountCreationParametersDto accountDto = parametersDto.getAccount();
    AccountCreationUseCase.Parameters parameters =
        new AccountCreationUseCase.Parameters(
            currentUser,
            new AccountNumber(accountDto.getAccountNumber()),
            new AccountBIK(accountDto.getBik()),
            new AccountINN(accountDto.getInn()),
            new AccountOwnerName(accountDto.getOwnerName()));
    return accountCreationUseCase
        .execute(parameters)
        .mapError(ignored -> HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private Result<PaymentBeacon, HttpStatus> createPaymentBeacon(
      AccountId accountId, PaymentAmount amount, UserId currentUser) {
    return beaconCreationUseCase
        .execute(new PaymentBeaconCreationUseCase.Parameters(accountId, amount, currentUser))
        .mapError(ignored -> HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private BeaconInfoDto toDto(PaymentBeacon paymentBeacon) {
    return conversionService.convert(paymentBeacon, BeaconInfoDto.class);
  }
}
