package ru.finess.finess.payment.application;

import com.github.sviperll.result4j.Result;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.finess.finess.common.application.UseCase;
import ru.finess.finess.payment.domain.PaymentBeacon;

@Service
@RequiredArgsConstructor
public class GettingPaymentBeaconUseCase implements UseCase<PaymentBeacon, GettingPaymentBeaconUseCase.NotFound, GettingPaymentBeaconUseCase.Parameters> {

  public record NotFound(int major, int minor) {}

  public record Parameters(int major, int minor) {}

  private final PaymentBeaconRepository paymentBeaconRepository;

  @Override
  public Result<PaymentBeacon, NotFound> execute(@NonNull Parameters parameters) {
    int major = parameters.major;
    int minor = parameters.minor;
    return paymentBeaconRepository.findByMajorMinor(major, minor)
        .map(Result::<PaymentBeacon, NotFound>success)
        .orElseGet(() -> Result.error(new NotFound(major, minor)));
  }

}
