package ru.finess.finess.payment.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Getter
@Entity
@Table(name = "payment_beacons")
@Accessors(fluent = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentBeacon {

  @EqualsAndHashCode.Include @EmbeddedId private PaymentBeaconId id;

  @AttributeOverride(
      name = "id",
      column = @Column(name = "account_id", nullable = false, updatable = false))
  private AccountId accountId;

  @AttributeOverride(
      name = "value",
      column = @Column(name = "amount", nullable = false, updatable = false))
  private PaymentAmount amount;

  private PaymentBeaconBluetoothId bluetoothId;

  @Column(name = "major", nullable = false, updatable = false)
  private int major;

  @Column(name = "minor", nullable = false, updatable = false)
  private int minor;

  public static PaymentBeacon of(
      @NonNull AccountId accountId,
      @NonNull PaymentAmount amount,
      PaymentBeaconBluetoothId bluetoothId,
      int major,
      int minor
  ) {
    return new PaymentBeacon(PaymentBeaconId.random(), accountId, amount, bluetoothId, major, minor);
  }

}
