package ru.finess.finess.payment.application;

import com.github.sviperll.result4j.Result;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.finess.finess.common.application.UseCase;
import ru.finess.finess.identity.domain.UserId;
import ru.finess.finess.payment.domain.Account;

@Component
@RequiredArgsConstructor
public class GettingAccountsUseCase
    implements UseCase<List<Account>, Void, GettingAccountsUseCase.Parameters> {

  private final AccountRepository accountRepository;

  public record Parameters(@NonNull UserId user) {}

  @Override
  public Result<List<Account>, Void> execute(@NonNull Parameters parameters) {
    List<Account> accounts = accountRepository.findAllForUser(parameters.user());
    return Result.success(accounts);
  }
}
