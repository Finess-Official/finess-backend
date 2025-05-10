package ru.finess.finess.payment.infrastructure;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Component;
import ru.finess.finess.payment.application.PaymentQrCodeRepository;

@Component
@RequiredArgsConstructor
public class RelationalPaymentQrCodeRepository implements PaymentQrCodeRepository {

  @Delegate(types = PaymentQrCodeRepository.class)
  private final JpaPaymentQrCodeRepository delegate;
}
