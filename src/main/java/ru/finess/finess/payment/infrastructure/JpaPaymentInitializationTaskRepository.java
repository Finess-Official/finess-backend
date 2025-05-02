package ru.finess.finess.payment.infrastructure;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.finess.finess.payment.domain.PaymentInitialization;
import ru.finess.finess.payment.domain.PaymentInitializationId;

public interface JpaPaymentInitializationTaskRepository
    extends JpaRepository<PaymentInitialization, PaymentInitializationId> {

  @Query(
      """
          select p from PaymentInitialization p
          where p.status = 'NEW' or (p.status = 'FAILED' and p.retryCount < :retryLimit)
          order by p.createdAt asc
          limit :batchSize
          """)
  List<PaymentInitialization> findAllUnprocessed(
      @Param("batchSize") int batchSize, @Param("retryLimit") int retryLimit);
}
