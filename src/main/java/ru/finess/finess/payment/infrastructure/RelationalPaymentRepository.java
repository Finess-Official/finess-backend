package ru.finess.finess.payment.infrastructure;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Component;
import ru.finess.finess.payment.application.PaymentRepository;
import ru.finess.finess.payment.domain.PaymentId;

@Component
@RequiredArgsConstructor
public class RelationalPaymentRepository implements PaymentRepository {

  @Delegate(types = PaymentRepository.class)
  private final JpaPaymentRepository delegate;

  public List<PaymentId> findUnprocessedPayments(int batchSize, int lastProcessedPayment) {
    return delegate.findUnprocessedPayments(batchSize, lastProcessedPayment).stream()
        .map(PaymentId::new)
        .collect(Collectors.toList());
  }
}
