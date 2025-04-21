package ru.finess.finess.payment.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.finess.finess.payment.domain.Account;
import ru.finess.finess.payment.domain.AccountId;

public interface JpaAccountRepository extends JpaRepository<Account, AccountId> {}
