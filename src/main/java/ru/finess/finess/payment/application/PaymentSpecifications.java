package ru.finess.finess.payment.application;

import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.data.jpa.domain.Specification;
import ru.finess.finess.identity.domain.UserId;
import ru.finess.finess.payment.domain.Account;
import ru.finess.finess.payment.domain.Payment;
import ru.finess.finess.payment.domain.PaymentAmount;
import ru.finess.finess.payment.domain.PaymentAmount_;
import ru.finess.finess.payment.domain.PaymentStatus;
import ru.finess.finess.payment.domain.Payment_;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PaymentSpecifications {

  public static class Builder {

    private Specification<Payment> specification;

    private Builder(UserId user) {
      this.specification =
          withRecipient(user)
              .or(withSender(user))
              .and(withStatus(PaymentStatus.RECIEVED).or(withStatus(PaymentStatus.FAILED)));
    }

    public Builder fromDate(@NonNull OffsetDateTime date) {
      this.specification = specification.and(withFromDate(date));
      return this;
    }

    public Builder toDate(@NonNull OffsetDateTime date) {
      this.specification = specification.and(withToDate(date));
      return this;
    }

    public Builder fromAmount(@NonNull PaymentAmount amount) {
      this.specification = specification.and(withFromAmount(amount));
      return this;
    }

    public Builder toAmount(@NonNull PaymentAmount amount) {
      this.specification = specification.and(withToAmount(amount));
      return this;
    }

    public Builder status(@NonNull PaymentStatus status) {
      this.specification = specification.and(withStatus(status));
      return this;
    }

    public Specification<Payment> build() {
      return specification;
    }
  }

  public static Builder builder(@NonNull UserId user) {
    return new Builder(user);
  }

  private static Specification<Payment> withRecipient(@NonNull UserId currentUser) {
    return (root, query, builder) -> {
      Subquery<Account> subquery = query.subquery(Account.class);
      Root<Account> accountRoot = subquery.from(Account.class);
      subquery
          .select(accountRoot)
          .where(
              builder.and(
                  builder.equal(accountRoot.get("ownerId"), currentUser),
                  builder.equal(accountRoot.get("id"), root.get(Payment_.recipientAccount))));

      return builder.and(
          builder.or(
              builder.equal(root.get(Payment_.status), PaymentStatus.RECIEVED),
              builder.equal(root.get(Payment_.status), PaymentStatus.FAILED)),
          builder.exists(subquery));
    };
  }

  private static Specification<Payment> withSender(@NonNull UserId currentUser) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.get(Payment_.initiator), currentUser);
  }

  private static Specification<Payment> withFromDate(@NonNull OffsetDateTime date) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.greaterThanOrEqualTo(root.get(Payment_.createdAt), date);
  }

  private static Specification<Payment> withToDate(@NonNull OffsetDateTime date) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.lessThanOrEqualTo(root.get(Payment_.createdAt), date);
  }

  private static Specification<Payment> withFromAmount(@NonNull PaymentAmount amount) {
    return (root, query, criteriaBuilder) -> {
      BigDecimal fromAmount = amount.value();
      return criteriaBuilder.greaterThanOrEqualTo(
          root.get(Payment_.amount).get(PaymentAmount_.VALUE), fromAmount);
    };
  }

  private static Specification<Payment> withToAmount(@NonNull PaymentAmount amount) {
    return (root, query, criteriaBuilder) -> {
      BigDecimal toAmount = amount.value();
      return criteriaBuilder.lessThanOrEqualTo(
          root.get(Payment_.amount).get(PaymentAmount_.VALUE), toAmount);
    };
  }

  private static Specification<Payment> withStatus(@NonNull PaymentStatus status) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.equal(root.get(Payment_.status), status);
  }
}
