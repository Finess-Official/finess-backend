package ru.finess.finess.payment.infrastructure;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.finess.finess.payment.domain.Payment;
import ru.finess.finess.payment.domain.PaymentId;

public interface JpaPaymentRepository
    extends JpaRepository<Payment, PaymentId>, JpaSpecificationExecutor<Payment> {

  @Query(
      value =
          """
    select nextval('payment_number_sq')
""",
      nativeQuery = true)
  int nextSequenceNumber();

  @Query(
      value =
          """
          select p.id from payments p
          where p.status in ('INITIALIZED', 'RECIEVING')
          and p.sequence_number > :lastProcessedPayment
          order by p.sequence_number asc
          limit :batchSize
      """,
      nativeQuery = true)
  List<UUID> findUnprocessedPayments(
      @Param("batchSize") int batchSize, @Param("lastProcessedPayment") int lastProcessedPayment);
}
