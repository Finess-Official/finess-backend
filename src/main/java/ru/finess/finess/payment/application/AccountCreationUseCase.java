package ru.finess.finess.payment.application;

import com.github.sviperll.result4j.Result;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.finess.finess.common.application.UseCase;
import ru.finess.finess.identity.domain.UserId;
import ru.finess.finess.payment.domain.*;

@Service
@RequiredArgsConstructor
public class AccountCreationUseCase
    implements UseCase<Account, Void, AccountCreationUseCase.Parameters> {

  private final AccountRepository accountRepository;

  public record Parameters(
      @NonNull UserId ownerId,
      @NonNull AccountNumber number,
      @NonNull AccountBIK bik,
      @NonNull AccountINN inn,
      @NonNull AccountOwnerName name) {}

  @Transactional
  @Override
  public Result<Account, Void> execute(@NonNull Parameters parameters) {
    Account account =
        Account.builder()
            .ownerId(parameters.ownerId())
            .inn(parameters.inn())
            .number(parameters.number())
            .bik(parameters.bik())
            .ownerName(parameters.name())
            .build();
    accountRepository.save(account);
    return Result.success(account);
  }
}
