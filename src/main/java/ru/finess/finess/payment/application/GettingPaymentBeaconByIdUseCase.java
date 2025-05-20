package ru.finess.finess.payment.application;

import com.github.sviperll.result4j.Result;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.finess.finess.common.application.UseCase;
import ru.finess.finess.payment.domain.PaymentBeacon;
import ru.finess.finess.payment.domain.PaymentBeaconId;

@Service
@RequiredArgsConstructor
public class GettingPaymentBeaconByIdUseCase
    implements UseCase<PaymentBeacon, GettingPaymentBeaconByIdUseCase.NotFound, PaymentBeaconId> {

  public record NotFound(PaymentBeaconId paymentBeaconId) {}

  private final PaymentBeaconRepository beaconRepository;

  @Override
  public Result<PaymentBeacon, GettingPaymentBeaconByIdUseCase.NotFound> execute(
      @NonNull PaymentBeaconId beaconId) {
    return beaconRepository
        .findActiveById(beaconId)
        .map(Result::<PaymentBeacon, NotFound>success)
        .orElseGet(() -> Result.error(new NotFound(beaconId)));
  }
}
