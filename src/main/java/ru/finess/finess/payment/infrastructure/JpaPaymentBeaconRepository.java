package ru.finess.finess.payment.infrastructure;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.finess.finess.identity.domain.UserId;
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

  @Query(
      """
    select pb from PaymentBeacon pb
      inner join Account a on a.id = pb.accountId
    where pb.major = :major
    and pb.minor = :minor
    and a.ownerId = :user
  """)
  Optional<PaymentBeacon> findByMajorMinorForUser(
      @Param("major") int major, @Param("minor") int minor, @Param("user") UserId userId);

  @Query(
      """
    select pb from PaymentBeacon pb
    where pb.major = :major
    and pb.minor = :minor
      and pb.isActive = true
  """)
  Optional<PaymentBeacon> findActiveByMajorMinor(@Param("major") int major, @Param("minor") int minor);

  @Query(
      """
    select pb from PaymentBeacon pb
    where pb.id = :id
    and pb.isActive = true
  """)
  Optional<PaymentBeacon> findActiveById(@Param("id") PaymentBeaconId beaconId);
}
