package ru.finess.finess.payment.infrastructure;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import org.springframework.stereotype.Component;
import ru.finess.finess.payment.application.AccountRepository;

@Component
@RequiredArgsConstructor
public class RelationalAccountRepository implements AccountRepository {

  @Delegate(types = AccountRepository.class)
  private final JpaAccountRepository delegate;
}
