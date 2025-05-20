package ru.finess.finess.payment.application;

import lombok.NonNull;
import ru.finess.finess.identity.domain.UserId;
import ru.finess.finess.payment.domain.PaymentBeacon;
import ru.finess.finess.payment.domain.PaymentBeaconId;

import java.util.Map;
import java.util.Optional;

public interface PaymentBeaconRepository {

  Map.Entry<Integer, Integer> generateSettings();

  void save(@NonNull PaymentBeacon beacon);

  Optional<PaymentBeacon> findByMajorMinorForUser(int major, int minor, UserId userId);

  Optional<PaymentBeacon> findActiveByMajorMinor(int major, int minor);

  Optional<PaymentBeacon> findActiveById(@NonNull PaymentBeaconId beaconId);
}
