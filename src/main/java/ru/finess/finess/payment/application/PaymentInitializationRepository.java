package ru.finess.finess.payment.application;

import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import ru.finess.finess.identity.domain.UserId;
import ru.finess.finess.payment.domain.PaymentInitialization;
import ru.finess.finess.payment.domain.PaymentInitializationId;

public interface PaymentInitializationRepository {

  void save(@NonNull PaymentInitialization initialization);

  List<PaymentInitialization> findAllUnprocessed(int batchSize, int retryLimit);

  Optional<PaymentInitialization> findForUser(
      @NonNull UserId userId, @NonNull PaymentInitializationId id);
}
