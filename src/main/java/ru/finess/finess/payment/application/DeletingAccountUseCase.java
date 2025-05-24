package ru.finess.finess.payment.application;

import com.github.sviperll.result4j.Result;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.finess.finess.common.application.UseCase;
import ru.finess.finess.identity.domain.UserId;
import ru.finess.finess.payment.domain.Account;
import ru.finess.finess.payment.domain.AccountId;

@Component
@RequiredArgsConstructor
public class DeletingAccountUseCase
    implements UseCase<Void, DeletingAccountUseCase.NotFound, DeletingAccountUseCase.Parameters> {

  public record Parameters(@NonNull UserId user, @NonNull AccountId account) {}

  public record NotFound(@NonNull UserId user, @NonNull AccountId account) {}

  private final AccountRepository accountRepository;

  @Override
  public Result<Void, NotFound> execute(@NonNull Parameters parameters) {
    return accountRepository
        .findByIdForUser(parameters.user, parameters.account)
        .map(this::deleteAccount)
        .orElseGet(() -> Result.error(new NotFound(parameters.user, parameters.account)));
  }

  private Result<Void, NotFound> deleteAccount(Account result) {
    accountRepository.delete(result);
    return Result.success(null);
  }
}
