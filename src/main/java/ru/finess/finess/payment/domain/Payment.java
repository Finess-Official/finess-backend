package ru.finess.finess.payment.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.*;
import lombok.experimental.Accessors;
import ru.finess.finess.identity.domain.UserId;

@Getter
@Entity
@Table(name = "payments")
@Accessors(fluent = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Payment {

  @EmbeddedId private PaymentId id;

  @Column(name = "sequence_number", nullable = false, updatable = false, unique = true)
  private int sequenceNumber;

  @Column(name = "status", nullable = false)
  @Enumerated(EnumType.STRING)
  private PaymentStatus status;

  @Column(name = "created_at", nullable = false, updatable = false)
  private OffsetDateTime createdAt;

  @AttributeOverride(
      name = "value",
      column = @Column(name = "amount", nullable = false, updatable = false))
  private PaymentAmount amount;

  @AttributeOverride(
      name = "value",
      column = @Column(name = "initiator_id", nullable = false, updatable = false))
  private UserId initiator;

  @AttributeOverride(
      name = "id",
      column = @Column(name = "recipient_account_id", nullable = false, updatable = false))
  private AccountId recipientAccount;

  @AttributeOverride(
      name = "value",
      column =
          @Column(
              name = "acquiring_payment_id",
              nullable = false,
              updatable = false,
              unique = true))
  private AcquiringPaymentId acquiringPaymentId;

  public static Payment initialized(
      int sequenceNumber,
      @NonNull OffsetDateTime createdAt,
      @NonNull PaymentAmount amount,
      @NonNull UserId initiator,
      @NonNull AccountId recipientAccount,
      @NonNull AcquiringPaymentId acquiringPaymentId) {
    return new Payment(
        PaymentId.random(),
        sequenceNumber,
        PaymentStatus.INITIALIZED,
        createdAt,
        amount,
        initiator,
        recipientAccount,
        acquiringPaymentId);
  }

  public void receiving() {
    if (status == PaymentStatus.RECIEVING) {
      return; // idempotency
    }
    if (status == PaymentStatus.INITIALIZED) {
      this.status = PaymentStatus.RECIEVING;
    } else {
      throw new IllegalStateException(
          String.format("Unexpected payment status for receiving payment (%s): %s", id, status));
    }
  }

  public void received() {
    if (status == PaymentStatus.RECIEVED) {
      return; // idempotency
    }
    if (status == PaymentStatus.RECIEVING || status == PaymentStatus.INITIALIZED) {
      this.status = PaymentStatus.RECIEVED;
    } else {
      throw new IllegalStateException(
          String.format("Unexpected payment status for completing payment (%s): %s", id, status));
    }
  }

  public void sending() {
    if (status == PaymentStatus.SENDING) {
      return; // idempotency
    }
    if (status == PaymentStatus.RECIEVED) {
      this.status = PaymentStatus.SENDING;
    } else {
      throw new IllegalStateException(
          String.format("Unexpected payment status for sending payment (%s): %s", id, status));
    }
  }

  public void sent() {
    if (status == PaymentStatus.SENT) {
      return; // idempotency
    }
    if (status == PaymentStatus.SENDING) {
      this.status = PaymentStatus.SENT;
    } else {
      throw new IllegalStateException(
          String.format("Unexpected payment status for completing payment (%s): %s", id, status));
    }
  }

  public void failed() {
    if (status == PaymentStatus.FAILED) {
      return; // idempotency
    }
    switch (status) {
      case RECIEVING, RECIEVED, INITIALIZED, SENDING -> this.status = PaymentStatus.FAILED;
      default ->
          throw new IllegalStateException(
              String.format("Unexpected payment status for failing payment (%s): %s", id, status));
    }
  }

  public void canceled() {
    if (status == PaymentStatus.CANCELED) {
      return; // idempotency
    }
    switch (status) {
      case RECIEVING, INITIALIZED -> this.status = PaymentStatus.CANCELED;
      default ->
          throw new IllegalStateException(
              String.format(
                  "Unexpected payment status for canceling payment (%s): %s", id, status));
    }
  }
}
