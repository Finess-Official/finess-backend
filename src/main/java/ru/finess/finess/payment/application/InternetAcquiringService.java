package ru.finess.finess.payment.application;

import java.net.URI;
import java.util.concurrent.CompletableFuture;
import lombok.NonNull;
import ru.finess.finess.payment.domain.PaymentAmount;
import ru.finess.finess.payment.domain.PaymentInitializationId;

public interface InternetAcquiringService {

  CompletableFuture<URI> initializePayment(
      @NonNull PaymentAmount paymentAmount, @NonNull PaymentInitializationId id);
}
