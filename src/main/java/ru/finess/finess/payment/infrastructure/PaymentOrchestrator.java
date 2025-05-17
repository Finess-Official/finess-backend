package ru.finess.finess.payment.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import ru.finess.finess.payment.domain.*;
import ru.finess.finess.payment.domain.event.PaymentInitializedEvent;

@Component
@RequiredArgsConstructor
public class PaymentOrchestrator {

  private final JpaPaymentInitializationTaskRepository initializationRepository;
  private final JpaPaymentRepository paymentRepository;
  private final JpaPaymentQrCodeRepository qrCodeRepository;

  @TransactionalEventListener(
      classes = PaymentInitializedEvent.class,
      fallbackExecution = true,
      phase = TransactionPhase.BEFORE_COMMIT)
  public void onPaymentInitialized(PaymentInitializedEvent event) {
    PaymentInitialization paymentInitialization = getInitialization(event);
    PaymentQrCode qrCode = getQrCode(paymentInitialization);

    Payment payment =
        Payment.initialized(
            paymentRepository.nextSequenceNumber(),
            paymentInitialization.createdAt(),
            qrCode.amount(),
            paymentInitialization.initiator(),
            qrCode.accountId(),
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

  private PaymentQrCode getQrCode(PaymentInitialization paymentInitialization) {
    PaymentQrCodeId qrCodeId = paymentInitialization.qrCodeId();
    return qrCodeRepository
        .findById(qrCodeId)
        .orElseThrow(() -> new IllegalStateException("PaymentQrCode not found: " + qrCodeId));
  }
}
