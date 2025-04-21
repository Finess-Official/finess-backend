package ru.finess.finess.payment.application;

import lombok.NonNull;
import ru.finess.finess.payment.domain.Account;

public interface AccountRepository {

  void save(@NonNull Account account);
}
