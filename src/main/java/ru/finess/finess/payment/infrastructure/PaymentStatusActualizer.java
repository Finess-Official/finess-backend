package ru.finess.finess.payment.infrastructure;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.finess.finess.common.application.TransactionalWrapper;
import ru.finess.finess.payment.application.InternetAcquiringService;
import ru.finess.finess.payment.application.PaymentRepository;
import ru.finess.finess.payment.domain.Payment;
import ru.finess.finess.payment.domain.PaymentId;

@Slf4j
@Component
public class PaymentStatusActualizer {

  private final PaymentRepository paymentRepository;
  private final InternetAcquiringService internetAcquiringService;
  private final TransactionalWrapper wrapper;
  private final int batchSize;

  public PaymentStatusActualizer(
      PaymentRepository paymentRepository,
      InternetAcquiringService internetAcquiringService,
      TransactionalWrapper wrapper,
      @Value("${finess.payment.status.actualizer.batchSize}") int batchSize) {
    this.paymentRepository = paymentRepository;
    this.internetAcquiringService = internetAcquiringService;
    this.wrapper = wrapper;
    this.batchSize = batchSize;
  }

  @Scheduled(fixedDelayString = "${finess.payment.status.actualizer.fixedDelay}")
  public void actualize() {
    List<PaymentId> payments = paymentRepository.findUnprocessedPayments(batchSize, 0);
    int lastProcessed = updateStatuses(payments);
    do {
      payments = paymentRepository.findUnprocessedPayments(batchSize, lastProcessed);
      lastProcessed = updateStatuses(payments);
    } while (payments.size() == batchSize);
  }

  private int updateStatuses(List<PaymentId> paymentIds) {
    return wrapper.runInTransaction(
        () -> {
          List<Payment> payments = paymentRepository.findAllById(paymentIds);
          List<CompletableFuture<Void>> futures =
              payments.stream().map(this::updateStatus).toList();
          CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
          return getLastProcessedNumber(payments);
        });
  }

  private CompletableFuture<Void> updateStatus(Payment payment) {
    return internetAcquiringService
        .getPaymentStatus(payment.acquiringPaymentId())
        .thenAccept(
            status -> {
              switch (status) {
                case INITIALIZED -> pass();
                case RECIEVING -> payment.receiving();
                case RECIEVED -> payment.received();
                case CANCELED -> payment.canceled();
                case FAILED -> payment.failed();
                default -> throw new IllegalStateException("Unexpected value: " + status);
              }
            })
        .exceptionally(
            throwable -> {
              log.error("Unexpected error", throwable);
              payment.failed();
              return null;
            });
  }

  private int getLastProcessedNumber(List<Payment> payments) {
    if (payments.isEmpty()) {
      return 0;
    }
    return payments.getLast().sequenceNumber();
  }

  private void pass() {}
}
