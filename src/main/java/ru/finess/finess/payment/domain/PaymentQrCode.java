package ru.finess.finess.payment.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

@Getter
@Entity
@Table(name = "payment_qrcodes")
@Accessors(fluent = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentQrCode {

  @EqualsAndHashCode.Include @EmbeddedId private PaymentQrCodeId id;

  @AttributeOverride(
      name = "id",
      column = @Column(name = "account_id", nullable = false, updatable = false))
  private AccountId accountId;

  private PaymentAmount amount;

  public static PaymentQrCode of(
      @NonNull PaymentQrCodeId qrCodeId,
      @NonNull AccountId accountId,
      @NonNull PaymentAmount amount) {
    return new PaymentQrCode(qrCodeId, accountId, amount);
  }
}
