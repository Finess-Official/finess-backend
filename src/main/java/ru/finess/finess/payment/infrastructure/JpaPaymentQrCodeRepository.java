package ru.finess.finess.payment.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.finess.finess.payment.domain.PaymentQrCode;
import ru.finess.finess.payment.domain.PaymentQrCodeId;

public interface JpaPaymentQrCodeRepository extends JpaRepository<PaymentQrCode, PaymentQrCodeId> {

  @Query(
      value =
          """
        select nextval('payment_qrcode_id_sq')
        """,
      nativeQuery = true)
  long getNextSequenceNumber();
}
