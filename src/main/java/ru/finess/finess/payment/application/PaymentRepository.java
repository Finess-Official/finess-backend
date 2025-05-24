package ru.finess.finess.payment.application;

import java.util.List;
import lombok.NonNull;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import ru.finess.finess.payment.domain.Payment;
import ru.finess.finess.payment.domain.PaymentId;

public interface PaymentRepository {

  List<PaymentId> findUnprocessedPayments(int batchSize, int lastProcessedPayment);

  List<Payment> findAllById(Iterable<PaymentId> ids);

  List<Payment> findAll(@NonNull Specification<Payment> specification, @NonNull Sort sort);
}
