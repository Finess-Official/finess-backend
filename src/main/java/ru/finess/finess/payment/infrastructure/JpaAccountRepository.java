package ru.finess.finess.payment.infrastructure;

import java.util.List;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.finess.finess.identity.domain.UserId;
import ru.finess.finess.payment.domain.Account;
import ru.finess.finess.payment.domain.AccountId;

public interface JpaAccountRepository extends JpaRepository<Account, AccountId> {

  @Query(
      """
        select count(a) > 0 from Account a
        where a.id = :id and a.ownerId = :userId
        """)
  boolean existsForUser(@Param("userId") UserId user, @Param("id") @NonNull AccountId accountId);

  @Query(
      """
    select a from Account a
    where a.id = :account and a.ownerId = :user
  """)
  Optional<Account> findByIdForUser(
      @Param("user") UserId user, @Param("account") AccountId accountId);

  @Query(
      """
        select a from Account a
        where a.ownerId = :user
      """)
  List<Account> findAllForUser(@Param("user") UserId user);
}
