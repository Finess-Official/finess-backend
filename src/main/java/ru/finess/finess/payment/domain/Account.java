package ru.finess.finess.payment.domain;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import ru.finess.finess.identity.domain.UserId;

@Getter
@Entity
@Table(name = "accounts")
@Accessors(fluent = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Account {

  @EqualsAndHashCode.Include @EmbeddedId private AccountId id;

  @AttributeOverride(
      name = "value",
      column = @Column(name = "owner_id", nullable = false, updatable = false))
  private UserId ownerId;

  private AccountOwnerName ownerName;

  private AccountINN inn;

  private AccountBIK bik;

  private AccountNumber number;

  @Builder
  private static Account of(
      @NonNull UserId ownerId,
      @NonNull AccountOwnerName ownerName,
      @NonNull AccountINN inn,
      @NonNull AccountBIK bik,
      @NonNull AccountNumber number) {
    return new Account(AccountId.random(), ownerId, ownerName, inn, bik, number);
  }
}
