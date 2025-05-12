package ru.finess.finess.payment.application;

import com.github.sviperll.result4j.Result;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.finess.finess.common.application.UseCase;
import ru.finess.finess.payment.domain.Account;
import ru.finess.finess.payment.domain.AccountId;

@Service
@RequiredArgsConstructor
public class GettingAccountUseCase
    implements UseCase<Account, GettingAccountUseCase.NotFound, AccountId> {

  public record NotFound(AccountId id) {}

  private final AccountRepository accountRepository;

  @Override
  public Result<Account, NotFound> execute(@NonNull AccountId accountId) {
    Optional<Account> optionalAccount = accountRepository.findById(accountId);
    return Result.fromOptional(optionalAccount, new NotFound(accountId));
  }
}
