package ru.finess.finess.payment.application;

import lombok.NonNull;
import ru.finess.finess.payment.domain.PaymentBeacon;

import java.util.Map;

public interface PaymentBeaconRepository {

  Map.Entry<Integer, Integer> generateSettings();

  void save(@NonNull PaymentBeacon beacon);
}
