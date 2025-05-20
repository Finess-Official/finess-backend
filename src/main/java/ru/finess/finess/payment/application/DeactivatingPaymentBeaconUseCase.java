package ru.finess.finess.payment.application;

import com.github.sviperll.result4j.Result;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.finess.finess.common.application.UseCase;
import ru.finess.finess.identity.domain.UserId;

@Service
@RequiredArgsConstructor
public class DeactivatingPaymentBeaconUseCase
    implements UseCase<
        Void,
        DeactivatingPaymentBeaconUseCase.BeaconNotFound,
        DeactivatingPaymentBeaconUseCase.Parameters> {

  public record BeaconNotFound(int major, int minor) {}

  public record Parameters(int major, int minor, @NonNull UserId currentUser) {}

  private final PaymentBeaconRepository beaconRepository;

  @Transactional
  @Override
  public Result<Void, BeaconNotFound> execute(@NonNull Parameters parameters) {
    int major = parameters.major;
    int minor = parameters.minor;
    return beaconRepository
        .findByMajorMinorForUser(major, minor, parameters.currentUser)
        .map(
            paymentBeacon -> {
              paymentBeacon.deactivate();
              return Result.<Void, BeaconNotFound>success(null);
            })
        .orElseGet(() -> Result.error(new BeaconNotFound(major, minor)));
  }
}
