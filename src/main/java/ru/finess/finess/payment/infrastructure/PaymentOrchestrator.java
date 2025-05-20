package ru.finess.finess.payment.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import ru.finess.finess.payment.domain.Payment;
import ru.finess.finess.payment.domain.PaymentInitialization;
import ru.finess.finess.payment.domain.PaymentInitializationId;
import ru.finess.finess.payment.domain.event.PaymentInitializedEvent;

@Component
@RequiredArgsConstructor
public class PaymentOrchestrator {

  private final JpaPaymentInitializationTaskRepository initializationRepository;
  private final JpaPaymentRepository paymentRepository;

  @TransactionalEventListener(
      classes = PaymentInitializedEvent.class,
      fallbackExecution = true,
      phase = TransactionPhase.BEFORE_COMMIT)
  public void onPaymentInitialized(PaymentInitializedEvent event) {
    PaymentInitialization paymentInitialization = getInitialization(event);

    Payment payment =
        Payment.initialized(
            paymentRepository.nextSequenceNumber(),
            paymentInitialization.createdAt(),
            paymentInitialization.amount(),
            paymentInitialization.initiator(),
            paymentInitialization.accountId(),
            paymentInitialization.acquiringPaymentId());
    paymentRepository.save(payment);
  }

  private PaymentInitialization getInitialization(PaymentInitializedEvent event) {
    PaymentInitializationId paymentInitializationId = event.paymentInitializationId();
    return initializationRepository
        .findById(paymentInitializationId)
        .orElseThrow(
            () ->
                new IllegalStateException(
                    "PaymentInitialization not found: " + paymentInitializationId));
  }
}
