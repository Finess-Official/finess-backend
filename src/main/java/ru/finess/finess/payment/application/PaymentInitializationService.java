package ru.finess.finess.payment.application;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.finess.finess.payment.domain.AcquiringPayment;
import ru.finess.finess.payment.domain.PaymentInitialization;

@Slf4j
@Service
public class PaymentInitializationService {

  private final int batchSize;
  private final int retryLimit;
  private final PaymentInitializationRepository initializationRepository;
  private final InternetAcquiringService internetAcquiringService;

  public PaymentInitializationService(
      PaymentInitializationRepository initializationRepository,
      @Value("${finess.payment.initialization.batchSize}") int batchSize,
      @Value("${finess.payment.initialization.retryLimit}") int retryLimit,
      InternetAcquiringService internetAcquiringService) {
    this.batchSize = batchSize;
    this.retryLimit = retryLimit;
    this.initializationRepository = initializationRepository;
    this.internetAcquiringService = internetAcquiringService;
  }

  @Transactional(propagation = Propagation.REQUIRES_NEW)
  public void enqueue(@NonNull PaymentInitialization paymentInitialization) {
    initializationRepository.save(paymentInitialization);
  }

  @SneakyThrows
  @Transactional
  @Scheduled(fixedDelayString = "${finess.payment.initialization.fixedDelay}")
  protected void processInitializations() {
    List<PaymentInitialization> unprocessedInitializations = findUnprocessedInitializations();

    List<CompletableFuture<Void>> futures =
        unprocessedInitializations.stream().map(this::initializePayment).toList();
    CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
  }

  private List<PaymentInitialization> findUnprocessedInitializations() {
    return initializationRepository.findAllUnprocessed(batchSize, retryLimit).stream()
        .filter(initialization -> initialization.canProcess(retryLimit))
        .toList();
  }

  @SneakyThrows
  private CompletableFuture<Void> initializePayment(PaymentInitialization paymentInitialization) {
    paymentInitialization.process(retryLimit);

    CompletableFuture<AcquiringPayment> uriCompletableFuture =
        internetAcquiringService.initializePayment(
            paymentInitialization.amount(), paymentInitialization.id());

    return uriCompletableFuture
        .thenAccept(
            acquiringPayment -> {
              paymentInitialization.complete(acquiringPayment);

              // TODO: replace with ddd-like event dispatcher so that explicit domain events are not
              // processed through Spring
              initializationRepository.save(paymentInitialization);
            })
        .exceptionally(
            throwable -> {
              log.error(
                  "Failed to initialize payment for initialization {}. Error: {}",
                  paymentInitialization.id(),
                  throwable.toString());
              paymentInitialization.fail();
              return null;
            });
  }
}
