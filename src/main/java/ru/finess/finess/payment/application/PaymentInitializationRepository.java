package ru.finess.finess.payment.application;

import java.util.List;
import lombok.NonNull;
import ru.finess.finess.payment.domain.PaymentInitialization;

public interface PaymentInitializationRepository {

  void save(@NonNull PaymentInitialization initialization);

  List<PaymentInitialization> findAllUnprocessed(int batchSize, int retryLimit);
}
