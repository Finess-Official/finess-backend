package ru.finess.finess.payment.presentation;

import com.github.sviperll.result4j.Result;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.ConversionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.finess.finess.identity.domain.UserId;
import ru.finess.finess.payment.application.AccountCreationUseCase;
import ru.finess.finess.payment.application.DeactivatingPaymentBeaconUseCase;
import ru.finess.finess.payment.application.GettingPaymentBeaconUseCase;
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
public class BeaconsRestApi implements BeaconsApi {

  private final UUID bluetoothId;
  private final ConversionService conversionService;
  private final AccountCreationUseCase accountCreationUseCase;
  private final PaymentBeaconCreationUseCase beaconCreationUseCase;
  private final DeactivatingPaymentBeaconUseCase deactivatingPaymentBeaconUseCase;
  private final GettingPaymentBeaconUseCase gettingPaymentBeaconUseCase;
  private final Supplier<Optional<UserId>> currentUserSupplier;

  public BeaconsRestApi(
      @Value("${finess.payment.beacon.bluetoothId}") String bluetoothId,
      ConversionService conversionService,
      AccountCreationUseCase accountCreationUseCase,
      PaymentBeaconCreationUseCase beaconCreationUseCase,
      DeactivatingPaymentBeaconUseCase deactivatingPaymentBeaconUseCase,
      GettingPaymentBeaconUseCase gettingPaymentBeaconUseCase,
      Supplier<Optional<UserId>> currentUserSupplier) {
    this.bluetoothId = UUID.fromString(bluetoothId);
    this.conversionService = conversionService;
    this.accountCreationUseCase = accountCreationUseCase;
    this.beaconCreationUseCase = beaconCreationUseCase;
    this.deactivatingPaymentBeaconUseCase = deactivatingPaymentBeaconUseCase;
    this.gettingPaymentBeaconUseCase = gettingPaymentBeaconUseCase;
    this.currentUserSupplier = currentUserSupplier;
  }

  @Override
  public ResponseEntity<BeaconInfoDto> createBeacon(BeaconCreationRequestDto parameters) {
    UserId currentUser = getCurrentUser();
    PaymentAmount amount = new PaymentAmount(BigDecimal.valueOf(parameters.getAmount()));

    return createAccount(parameters, currentUser)
        .flatMap(account -> createPaymentBeacon(account.id(), amount, currentUser))
        .map(this::toDto)
        .map(ResponseEntity::ok)
        .recoverError(status -> ResponseEntity.status(status).build());
  }

  @Override
  public ResponseEntity<Void> deactivateBeacon(Integer major, Integer minor) {
    DeactivatingPaymentBeaconUseCase.Parameters parameters =
        new DeactivatingPaymentBeaconUseCase.Parameters(major, minor, getCurrentUser());
    return deactivatingPaymentBeaconUseCase
        .execute(parameters)
        .map(t -> ResponseEntity.noContent().<Void>build())
        .recoverError(notFound -> ResponseEntity.notFound().build());
  }

  @Override
  public ResponseEntity<BeaconInfoDto> findBeaconPaymentInfo(Integer major, Integer minor) {
    GettingPaymentBeaconUseCase.Parameters parameters =
        new GettingPaymentBeaconUseCase.Parameters(major, minor);
    return gettingPaymentBeaconUseCase
        .execute(parameters)
        .map(this::toDto)
        .map(ResponseEntity::ok)
        .recoverError(notFound -> ResponseEntity.notFound().build());
  }

  @Override
  public ResponseEntity<BeaconConfigurationDto> getBeaconConfiguration() {
    return ResponseEntity.ok(new BeaconConfigurationDto().bluetoothId(bluetoothId));
  }

  private UserId getCurrentUser() {
    return currentUserSupplier
        .get()
        .orElseThrow(() -> new SecurityException("Current user is not authenticated"));
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
