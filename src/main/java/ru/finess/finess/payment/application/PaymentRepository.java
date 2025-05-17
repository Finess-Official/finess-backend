package ru.finess.finess.payment.application;

import java.util.List;
import ru.finess.finess.payment.domain.Payment;
import ru.finess.finess.payment.domain.PaymentId;

public interface PaymentRepository {

  List<PaymentId> findUnprocessedPayments(int batchSize, int lastProcessedPayment);

  List<Payment> findAllById(Iterable<PaymentId> ids);
}
