package ru.finess.finess.payment.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.Accessors;
import org.springframework.data.domain.AbstractAggregateRoot;
import ru.finess.finess.identity.domain.UserId;
import ru.finess.finess.payment.domain.event.PaymentInitializedEvent;

@Getter
@Entity
@Table(name = "payment_initializations")
@Accessors(fluent = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PaymentInitialization extends AbstractAggregateRoot<PaymentInitialization> {

  @EqualsAndHashCode.Include @EmbeddedId private PaymentInitializationId id;

  @Column(name = "status", nullable = false)
  @Enumerated(EnumType.STRING)
  private PaymentInitializationStatus status;

  @Column(name = "acquiring_payment_url", unique = true)
  private String acquiringPaymentUrl;

  @AttributeOverride(name = "value", column = @Column(name = "acquiring_payment_id", unique = true))
  private AcquiringPaymentId acquiringPaymentId;

  @AttributeOverride(
      name = "value",
      column = @Column(name = "initiator_id", nullable = false, updatable = false))
  private UserId initiator;

  @AttributeOverride(
      name = "value",
      column = @Column(name = "qr_code_id", nullable = false, updatable = false))
  private PaymentQrCodeId qrCodeId;

  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  @Column(name = "retry_count", nullable = false)
  private int retryCount;

  public static PaymentInitialization of(
      @NonNull OffsetDateTime createdAt,
      @NonNull UserId initiator,
      @NonNull PaymentQrCodeId qrCodeId) {
    return new PaymentInitialization(
        PaymentInitializationId.random(),
        PaymentInitializationStatus.NEW,
        null,
        null,
        initiator,
        qrCodeId,
        createdAt,
        createdAt,
        0);
  }

  public void process(int retryLimit) {
    if (canProcess(retryLimit)) {
      status = PaymentInitializationStatus.IN_PROGRESS;
      updatedAt = OffsetDateTime.now();
    } else {
      throw new IllegalStateException(
          "Could not process initialization %s not in NEW or FAILED state. Actual: %s"
              .formatted(id, status));
    }
  }

  public boolean canProcess(int retryLimit) {
    return status == PaymentInitializationStatus.NEW
        || (status == PaymentInitializationStatus.FAILED && retryLimit < retryCount);
  }

  public void fail() {
    if (status == PaymentInitializationStatus.IN_PROGRESS) {
      status = PaymentInitializationStatus.FAILED;
      updatedAt = OffsetDateTime.now();
      retryCount++;
    } else {
      throw new IllegalStateException(
          "Could not fail initialization %s not in IN_PROGRESS state. Actual: %s"
              .formatted(id, status));
    }
  }

  public void complete(@NonNull AcquiringPayment acquiringPayment) {
    if (status == PaymentInitializationStatus.IN_PROGRESS) {
      this.status = PaymentInitializationStatus.INITIALIZED;
      this.acquiringPaymentUrl = acquiringPayment.paymentLink().toString();
      this.acquiringPaymentId = acquiringPayment.id();
      this.updatedAt = OffsetDateTime.now();

      registerEvent(new PaymentInitializedEvent(id));
    } else {
      throw new IllegalStateException(
          "Could not complete initialization %s not in IN_PROGRESS state. Actual: %s"
              .formatted(id, status));
    }
  }
}
