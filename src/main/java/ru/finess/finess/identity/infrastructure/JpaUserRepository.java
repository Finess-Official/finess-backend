package ru.finess.finess.identity.infrastructure;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.finess.finess.identity.domain.User;
import ru.finess.finess.identity.domain.UserId;
import ru.finess.finess.payment.domain.AccountId;

public interface JpaUserRepository extends JpaRepository<User, UserId> {
  @Query(
      """
      select u from User u
        inner join Account a on u.id = a.ownerId
      where a.id = :accountId
    """)
  Optional<User> findWithAccount(@Param("accountId") AccountId accountId);
}
