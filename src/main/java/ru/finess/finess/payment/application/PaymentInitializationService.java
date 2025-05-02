package ru.finess.finess.payment.application;

import java.net.URI;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.finess.finess.payment.domain.PaymentInitialization;
import ru.finess.finess.payment.domain.PaymentQrCode;
import ru.finess.finess.payment.domain.PaymentQrCodeId;

@Slf4j
@Service
public class PaymentInitializationService {

  private final int batchSize;
  private final int retryLimit;
  private final PaymentInitializationRepository initializationRepository;
  private final PaymentQrCodeRepository paymentQrCodeRepository;
  private final InternetAcquiringService internetAcquiringService;

  public PaymentInitializationService(
      PaymentInitializationRepository initializationRepository,
      @Value("${finess.payment.initialization.batchSize}") int batchSize,
      @Value("${finess.payment.initialization.retryLimit}") int retryLimit,
      PaymentQrCodeRepository paymentQrCodeRepository,
      InternetAcquiringService internetAcquiringService) {
    this.batchSize = batchSize;
    this.retryLimit = retryLimit;
    this.initializationRepository = initializationRepository;
    this.paymentQrCodeRepository = paymentQrCodeRepository;
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
    Map<PaymentQrCodeId, PaymentQrCode> qrCodes = findAllQrCodes(unprocessedInitializations);

    List<CompletableFuture<Void>> futures =
        unprocessedInitializations.stream()
            .map(paymentInitialization -> initializePayment(paymentInitialization, qrCodes))
            .toList();
    CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).join();
  }

  private List<PaymentInitialization> findUnprocessedInitializations() {
    return initializationRepository.findAllUnprocessed(batchSize, retryLimit).stream()
        .filter(PaymentInitialization::canProcess)
        .toList();
  }

  private Map<PaymentQrCodeId, PaymentQrCode> findAllQrCodes(
      List<PaymentInitialization> unprocessedInitializations) {
    Set<PaymentQrCodeId> qrCodeIds =
        unprocessedInitializations.stream()
            .map(PaymentInitialization::qrCodeId)
            .collect(Collectors.toSet());

    return paymentQrCodeRepository.findAllById(qrCodeIds).stream()
        .collect(Collectors.toMap(PaymentQrCode::id, Function.identity()));
  }

  @SneakyThrows
  private CompletableFuture<Void> initializePayment(
      PaymentInitialization paymentInitialization, Map<PaymentQrCodeId, PaymentQrCode> qrCodes) {
    paymentInitialization.process();

    PaymentQrCode qrCode = qrCodes.get(paymentInitialization.qrCodeId());
    if (Objects.isNull(qrCode)) {
      log.error(
          "QR code {} not found for payment initialization: {}",
          paymentInitialization.qrCodeId(),
          paymentInitialization.id());

      paymentInitialization.fail();
      return CompletableFuture.failedFuture(new RuntimeException("QR code not found"));
    }

    CompletableFuture<URI> uriCompletableFuture =
        internetAcquiringService.initializePayment(qrCode.amount(), paymentInitialization.id());

    return uriCompletableFuture
        .thenAccept(paymentInitialization::complete)
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
