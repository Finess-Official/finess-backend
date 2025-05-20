package ru.finess.finess.payment.application;

import com.github.sviperll.result4j.Result;
import java.util.Map;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.finess.finess.common.application.UseCase;
import ru.finess.finess.identity.domain.UserId;
import ru.finess.finess.payment.domain.AccountId;
import ru.finess.finess.payment.domain.PaymentAmount;
import ru.finess.finess.payment.domain.PaymentBeacon;
import ru.finess.finess.payment.domain.PaymentBeaconBluetoothId;

@Service
public class PaymentBeaconCreationUseCase
    implements UseCase<
        PaymentBeacon,
        PaymentBeaconCreationUseCase.AccountNotFound,
        PaymentBeaconCreationUseCase.Parameters> {

  private final PaymentBeaconBluetoothId bluetoothId;
  private final PaymentBeaconRepository beaconRepository;
  private final AccountRepository accountRepository;

  public record AccountNotFound(AccountId accountId) {}

  public record Parameters(
      @NonNull AccountId accountId, @NonNull PaymentAmount amount, @NonNull UserId currentUser) {}

  public PaymentBeaconCreationUseCase(
      @Value("${finess.payment.beacon.bluetoothId}") String bluetoothId,
      PaymentBeaconRepository beaconRepository,
      AccountRepository accountRepository) {
    this.bluetoothId = new PaymentBeaconBluetoothId(bluetoothId);
    this.beaconRepository = beaconRepository;
    this.accountRepository = accountRepository;
  }

  @Transactional
  @Override
  public Result<PaymentBeacon, AccountNotFound> execute(@NonNull Parameters parameters) {
    AccountId accountId = parameters.accountId();
    if (accountRepository.existsForUser(parameters.currentUser, accountId)) {
      Map.Entry<Integer, Integer> settings = beaconRepository.generateSettings();
      PaymentBeacon beacon =
          PaymentBeacon.ofActive(
              accountId,
              parameters.amount(),
              bluetoothId,
              settings.getKey(),
              settings.getValue());
      beaconRepository.save(beacon);
      return Result.success(beacon);
    } else {
      return Result.error(new AccountNotFound(accountId));
    }
  }
}
