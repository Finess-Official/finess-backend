package ru.finess.finess.payment.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.finess.finess.payment.domain.PaymentBeacon;
import ru.finess.finess.payment.domain.PaymentBeaconId;

public interface JpaPaymentBeaconRepository extends JpaRepository<PaymentBeacon, PaymentBeaconId> {

  @Query(
      value =
          """
      select nextval('payment_beacon_sq')
      """,
      nativeQuery = true)
  int generateUniqueMajorMinor();
}
