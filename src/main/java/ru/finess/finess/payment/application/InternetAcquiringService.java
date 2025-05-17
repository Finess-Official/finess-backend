package ru.finess.finess.payment.application;

import java.util.concurrent.CompletableFuture;
import lombok.NonNull;
import ru.finess.finess.payment.domain.AcquiringPayment;
import ru.finess.finess.payment.domain.AcquiringPaymentId;
import ru.finess.finess.payment.domain.PaymentAmount;
import ru.finess.finess.payment.domain.PaymentInitializationId;
import ru.finess.finess.payment.domain.PaymentStatus;

public interface InternetAcquiringService {

  CompletableFuture<AcquiringPayment> initializePayment(
      @NonNull PaymentAmount paymentAmount, @NonNull PaymentInitializationId id);

  CompletableFuture<PaymentStatus> getPaymentStatus(@NonNull AcquiringPaymentId acquiringPaymentId);
}
