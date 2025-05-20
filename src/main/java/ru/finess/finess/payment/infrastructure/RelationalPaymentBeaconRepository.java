package ru.finess.finess.payment.infrastructure;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Component;
import ru.finess.finess.payment.application.PaymentBeaconRepository;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class RelationalPaymentBeaconRepository implements PaymentBeaconRepository {

  @Delegate(types = PaymentBeaconRepository.class)
  private final JpaPaymentBeaconRepository jpaBeaconRepository;

  @Override
  public Map.Entry<Integer, Integer> generateSettings() {
    int majorMinor = jpaBeaconRepository.generateUniqueMajorMinor();
    int minor = majorMinor & 0xFFFF;
    int major = (majorMinor >> 16) & 0xFFFF;
    return Map.entry(major, minor);
  }
}
