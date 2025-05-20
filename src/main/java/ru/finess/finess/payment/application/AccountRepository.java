package ru.finess.finess.payment.application;

import java.util.Optional;
import lombok.NonNull;
import ru.finess.finess.identity.domain.UserId;
import ru.finess.finess.payment.domain.Account;
import ru.finess.finess.payment.domain.AccountId;

public interface AccountRepository {

  boolean existsForUser(@NonNull UserId user, @NonNull AccountId accountId);

  void save(@NonNull Account account);

  Optional<Account> findById(@NonNull AccountId id);

  boolean existsById(@NonNull AccountId accountId);
}
