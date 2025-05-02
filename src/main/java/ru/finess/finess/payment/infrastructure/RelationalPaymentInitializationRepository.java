package ru.finess.finess.payment.infrastructure;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Component;
import ru.finess.finess.payment.application.PaymentInitializationRepository;

@Component
@RequiredArgsConstructor
public class RelationalPaymentInitializationRepository implements PaymentInitializationRepository {

  @Delegate(types = PaymentInitializationRepository.class)
  private final JpaPaymentInitializationTaskRepository delegate;
}
